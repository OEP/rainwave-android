/*
 * Copyright (c) 2013, Paul M. Kilgo
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of Paul Kilgo nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cc.rainwave.android;

import java.io.IOException;
import java.net.HttpURLConnection;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.Album;
import cc.rainwave.android.api.types.RainwaveException;

public class SyncService extends IntentService {
    private static final String TAG = "SyncService";

    public SyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Session session = Session.getInstance(this);
        if(workIntent.getAction() == null) {
            Log.i(TAG, "No action specified -- ignoring.");
        }
        else if(workIntent.getAction().equals(ACTION_INFO)) {
            try {
                session.info();
                fetchArt(session);
                fetchStations(session);
                notifyUpdate(BROADCAST_EVENT_UPDATE);
            } catch (RainwaveException e) {
                handleException(e);
            }
        }
        else if(workIntent.getAction().equals(ACTION_SYNC)) {
            if(session.hasCredentials()) {
                try {
                    session.sync();
                    fetchArt(session);
                    fetchStations(session);
                    notifyUpdate(BROADCAST_EVENT_UPDATE);
                } catch (RainwaveException e) {
                    handleException(e);
                }
            }
            else {
                Log.i(TAG, "Not authenticated -- ignoring sync call.");
            }
        }
        else {
            Log.i(TAG, "Ignoring unknown action: " + workIntent.getAction());
        }
    }

    /** Send a global broadcast that the event has changed. */
    private void notifyUpdate(String action) {
        // FIXME: Change to LocalBroadcastManager
        Log.d(TAG, String.format("Sending '%s'.", action));
        sendBroadcast(new Intent(action));
    }

    private void handleException(RainwaveException exc) {
        Log.w(TAG, "Exception thrown", exc);
        switch(exc.getStatusCode()) {
        case HttpURLConnection.HTTP_FORBIDDEN:
            notifyUpdate(BROADCAST_REAUTHENTICATE);
            break;
        default:
            notifyUpdate(BROADCAST_EVENT_UPDATE_FAILED);
            break;
        }
    }

    /** Fetches the art for the current event. */
    private void fetchArt(Session session) {
        if(session.getCurrentEvent() == null) {
            Log.i(TAG, "No current event seen, not fetching album art.");
            return;
        }

        Album album = session.getCurrentEvent().getCurrentSong().getDefaultAlbum();
        if(album.getArt() != null && album.getArt().length() > 0) {
            try {
                session.fetchAlbumArt(album.getArt());
            } catch (IOException e) {
                Log.w(TAG, "Could not fetch album art.", e);
                session.clearCurrentAlbumArt();
            }
        }
        else {
            // no album art
            session.clearCurrentAlbumArt();
        }
    }

    /** Fetches station data if necessary. */
    private void fetchStations(Session session) {
        if(session.hasStations()) {
            return;
        }
        try {
            session.fetchStations();
        } catch (RainwaveException e) {
            Log.w(TAG, "Could not fetch station data", e);
        }
    }

    /** Send to the service to perform an immediate schedule lookup. */
    public static final String ACTION_INFO = "cc.rainwave.android.INFO";

    /** Send to the service to long-poll the remote server for an event change. */
    public static final String ACTION_SYNC = "cc.rainwave.android.SYNC";

    /** Sent by the service when schedule data has updated. */
    public static final String BROADCAST_EVENT_UPDATE = "cc.rainwave.android.EVENT_UPDATE";

    /** Sent by the service when schedule update failed. */
    public static final String BROADCAST_EVENT_UPDATE_FAILED = "cc.rainwave.android.EVENT_UPDATE_FAILED";

    /** Sent by service when authentication credentials are invalid. */
    public static final String BROADCAST_REAUTHENTICATE = "cc.rainwave.android.REAUTHENTICATE";
}

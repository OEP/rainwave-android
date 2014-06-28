package cc.rainwave.android;

import java.io.IOException;
import java.net.HttpURLConnection;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
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

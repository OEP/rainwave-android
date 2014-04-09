package cc.rainwave.android;

import java.io.IOException;

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
        Session session = Session.getInstance();
        if(workIntent.getAction() == null) {
            Log.i(TAG, "No action specified -- ignoring.");
        }
        else if(workIntent.getAction().equals(ACTION_INFO)) {
            try {
                session.info();
                fetchArt(session);
                fetchStations(session);
                notifyUpdate();
            } catch (RainwaveException e) {
                if(e.getCause() != null) {
                    Log.w(TAG, "Unknown exception thrown.", e.getCause());
                }
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else if(workIntent.getAction().equals(ACTION_SYNC)) {
            if(session.hasCredentials()) {
                try {
                    session.sync();
                    fetchArt(session);
                    fetchStations(session);
                    notifyUpdate();
                } catch (RainwaveException e) {
                    if(e.getCause() != null) {
                        Log.w(TAG, "Unknown exception thrown.", e.getCause());
                    }
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
    private void notifyUpdate() {
        // FIXME: Change to LocalBroadcastManager
        sendBroadcast(new Intent(BROADCAST_EVENT_UPDATE));
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
}

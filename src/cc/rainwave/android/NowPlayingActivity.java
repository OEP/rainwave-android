package cc.rainwave.android;

import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.ScheduleOrganizer;
import cc.rainwave.android.api.types.Song;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;

public class NowPlayingActivity extends Activity {
	private static final String TAG = "NowPlaying";
	
	private ScheduleOrganizer mOrganizer;
	
	private Session mSession;
	
	private FetchInfo mFetchInfo;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_nowplaying);
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if(mSession == null) {
            initializeSession();
        }
        
        fetchSchedules();
    }
    
    private void initializeSession() {
        try {
            mSession = Session.makeSession();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    private void fetchSchedules() {
        if(mSession == null) {
            // TODO: Some error here.
            return;
        }
        
        if(mFetchInfo == null) {
            mFetchInfo = new FetchInfo();
            mFetchInfo.execute(mSession);
        }
    }
    
    private void updateSchedule() {
    	if(mOrganizer == null) return;
    	
    	Song current = mOrganizer.getCurrentSong();
    	((TextView) findViewById(R.id.np_songTitle)).setText(current.song_title);
    	((TextView) findViewById(R.id.np_albumTitle)).setText(current.album_name);
    	((TextView) findViewById(R.id.np_artist)).setText(current.collapseArtists());
    	
    	// TODO: Make this happen somewhere other than the UI thread.
    	try {
    	    Drawable albumArt = mSession.fetchAlbumArt(current.album_art);
    	    ((ImageView) findViewById(R.id.np_albumArt)).setImageDrawable(albumArt);
    	}
    	catch(IOException e) {
    	    e.printStackTrace();
    	}
    }
    
    /**
     * Fetches the now playing info.
     * @author pkilgo
     *
     */
    protected class FetchInfo extends AsyncTask<Session, Integer, ScheduleOrganizer> {

        @Override
        protected ScheduleOrganizer doInBackground(Session... s) {
            try {
                return s[0].asyncGet();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        protected void onPostExecute(ScheduleOrganizer result) {
            super.onPostExecute(result);
            mOrganizer = result;
            updateSchedule();
            mFetchInfo = null;
        }
        
    }
}
package cc.rainwave.android;

import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.RainwaveException;
import cc.rainwave.android.api.types.RainwaveResponse;
import cc.rainwave.android.api.types.Song;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;

public class NowPlayingActivity extends Activity {
	private static final String TAG = "NowPlaying";
	
	private RainwaveResponse mOrganizer;
	
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
        initializeSession();
        fetchSchedules();
    }

    public void onPause() {
    	super.onPause();
    	stopTasks();
    }
    
    public void onStop() {
    	super.onStop();
    }
    
    public void onDestroy() {
    	super.onDestroy();
    }
    
    private void stopTasks() {
    	if(mFetchInfo != null) {
    		mFetchInfo.cancel(true);
    		mFetchInfo = null;
    	}
    }
    
    private void fetchSchedules() {
        fetchSchedules(false);
    }
    
    private void syncSchedules() {
        fetchSchedules(true);
    }
    
    private void fetchSchedules(boolean longPoll) {
        if(mSession == null) {
            // TODO: Some error here.
            return;
        }
        
        if(mFetchInfo == null) {
            mFetchInfo = new FetchInfo();
            mFetchInfo.execute(longPoll);
        }
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_preferences:
			Intent i = new Intent(this, RainwavePreferenceActivity.class);
			startActivity(i);
			break;
		}
		return false;
	}
    
    private void initializeSession() {
        try {
            mSession = Session.makeSession(this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateSchedule() {
    	if(mOrganizer == null) {
    	    // TODO: Some error here.
    	    return;
    	}
    	
    	Song current = mOrganizer.getCurrentSong();
    	((TextView) findViewById(R.id.np_songTitle)).setText(current.song_title);
    	((TextView) findViewById(R.id.np_albumTitle)).setText(current.album_name);
    	((TextView) findViewById(R.id.np_artist)).setText(current.collapseArtists());
    }
    
    private void updateAlbumArt(Bitmap art) {
        if(art == null) {
            // TODO: Some error here.
            art = BitmapFactory.decodeResource(getResources(), R.drawable.noart);
        }
        
        ((ImageView) findViewById(R.id.np_albumArt)).setImageBitmap(art);
    }
    
    /**
     * Fetches the now playing info.
     * @author pkilgo
     *
     */
    protected class FetchInfo extends AsyncTask<Boolean, Integer, Bundle> {
        private String TAG = "Unnamed";
        private boolean mLongPoll = false;

        @Override
        protected Bundle doInBackground(Boolean ... flags) {
            mLongPoll = flags[0];
            TAG = (mLongPoll) ? "LongPoll" : "AsyncPoll";
        	Log.d(TAG, "Fetching a schedule");
        	
            Bundle b = new Bundle();
            try {
                RainwaveResponse organizer =
                        (mLongPoll) ? mSession.syncGet() : mSession.asyncGet();
                        
                b.putParcelable(SCHEDULE, organizer);
                
                if(!organizer.hasError()) {
                    Song song = organizer.getCurrentSong();
                    Bitmap art = mSession.fetchAlbumArt(song.album_art);
                    b.putParcelable(ART, art);
                }

                return b;
            } catch (IOException e) {
                Log.e(TAG, "IOException occured: " + e);
                return null;
            } catch (RainwaveException e) {
            	Log.e(TAG, "API error: " + e.getMessage());
            	return null;
            }
            
        }
        
        protected void onPostExecute(Bundle result) {
            super.onPostExecute(result);
            mFetchInfo = null;
            
            // Was there an IO failure?
            if(result == null) {
                mFetchInfo = null;
            	return;
            }
            
            mOrganizer = result.getParcelable(SCHEDULE);
            
            updateSchedule();
            updateAlbumArt( (Bitmap) result.getParcelable(ART) );
            
            if(mSession.isAuthenticated()) {
                syncSchedules();
            }
            
            Log.d(TAG, "Exiting successfully.");
        }
    }
    
    public static final String
        SCHEDULE = "schedule",
        ART = "art";
}
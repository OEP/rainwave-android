package cc.rainwave.android;

import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.ScheduleOrganizer;
import cc.rainwave.android.api.types.Song;

import android.app.Activity;
import android.graphics.Bitmap;
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
            mFetchInfo.execute();
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
            return;
        }
        
        ((ImageView) findViewById(R.id.np_albumArt)).setImageBitmap(art);
    }
    
    /**
     * Fetches the now playing info.
     * @author pkilgo
     *
     */
    protected class FetchInfo extends AsyncTask<String, Integer, Bundle> {

        @Override
        protected Bundle doInBackground(String... s) {
            Bundle b = new Bundle();
            try {
                ScheduleOrganizer organizer = mSession.asyncGet();
                b.putParcelable(SCHEDULE, organizer);
                
                Song song = organizer.getCurrentSong();
                Bitmap art = mSession.fetchAlbumArt(song.album_art);
                b.putParcelable(ART, art);

                return b;
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return b;
        }
        
        protected void onPostExecute(Bundle result) {
            super.onPostExecute(result);
            
            if(result == null) return;
            
            mOrganizer = result.getParcelable(SCHEDULE);
            updateSchedule();
            updateAlbumArt( (Bitmap) result.getParcelable(ART) );
            mFetchInfo = null;
        }
        
        public static final String
            SCHEDULE = "schedule",
            ART = "art";
    }
}
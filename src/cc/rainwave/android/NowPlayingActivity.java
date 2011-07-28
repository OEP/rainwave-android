package cc.rainwave.android;

import java.io.IOException;
import java.net.MalformedURLException;

import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.Event;
import cc.rainwave.android.api.types.ScheduleOrganizer;
import cc.rainwave.android.api.types.Song;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class NowPlayingActivity extends Activity {
	private static final String TAG = "NowPlaying";
	
	private ScheduleOrganizer mOrganizer;
	
	private Session mSession;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_nowplaying);
        initialize();
    }
    
    private void initialize() {
    	try {
			mSession = Session.makeSession();
			mOrganizer = mSession.asyncGet();
			updateSchedule();
			Log.d(TAG, "Schedule gotten!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void updateSchedule() {
    	if(mOrganizer == null) return;
    	
    	Song current = mOrganizer.getCurrentSong();
    	((TextView) findViewById(R.id.np_songTitle)).setText(current.song_title);
    	((TextView) findViewById(R.id.np_albumTitle)).setText(current.album_name);
    	((TextView) findViewById(R.id.np_artist)).setText(current.collapseArtists());
    	
    	try {
    	    Drawable albumArt = mSession.fetchAlbumArt(current.album_art);
    	    ((ImageView) findViewById(R.id.np_albumArt)).setImageDrawable(albumArt);
    	}
    	catch(IOException e) {
    	    e.printStackTrace();
    	}
    }
}
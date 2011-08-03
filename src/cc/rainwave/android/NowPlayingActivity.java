package cc.rainwave.android;

import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.RainwaveException;
import cc.rainwave.android.api.types.RainwaveResponse;
import cc.rainwave.android.api.types.RatingResult;
import cc.rainwave.android.api.types.Song;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;

public class NowPlayingActivity extends Activity {
	private static final String TAG = "NowPlaying";
	
	private RainwaveResponse mOrganizer;
	
	private Session mSession;
	
	private FetchInfo mFetchInfo;
	
	private RateTask mRateTask;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_nowplaying);
        setListeners();
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
    
    public Dialog onCreateDialog(int id) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	switch(id) {
    	case DIALOG_RATE:
    		final RatingBar rating = new RatingBar(this);
    		rating.setStepSize(0.5f);
    		
    		return builder.setTitle(R.string.label_rateSong)
    			.setPositiveButton(R.string.label_rate, new OnClickListener() {
					@Override
					public void onClick(DialogInterface di, int which) {
						mRateTask = new RateTask();
						Song s = mOrganizer.getCurrentSong();
						float score = rating.getRating();
						mRateTask.execute(s.song_id, score);
					}
    			})
    			.setNegativeButton(R.string.label_cancel, null)
    			.setView(rating)
    			.create();
    		
    	default:
    		return builder.setMessage("Sorry! Your princess is in another castle!").create();
    	}
    }
    
    private void setListeners() {
    	OnTouchListener tmp = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				switch(e.getAction()) {
				case MotionEvent.ACTION_DOWN:
				    if(mSession.isAuthenticated()) {
    					showDialog(DIALOG_RATE);
    					return true;
				    }
				}
				return false;
			}
    	};
    	
    	((TextView) findViewById(R.id.np_songRating)).setOnTouchListener(tmp);
    	((TextView) findViewById(R.id.np_albumRating)).setOnTouchListener(tmp);
    }
    
    private void stopTasks() {
    	if(mFetchInfo != null) {
    		mFetchInfo.cancel(true);
    		mFetchInfo = null;
    	}
    	
    	if(mRateTask != null) {
    		mRateTask.cancel(true);
    		mRateTask = null;
    	}
    }
    
    private void fetchSchedules() {
        fetchSchedules(true);
    }
    
    private void syncSchedules() {
        fetchSchedules(false);
    }
    
    private void fetchSchedules(boolean init) {
        if(mSession == null) {
            // TODO: Some error here.
            return;
        }
        
        if(mFetchInfo == null) {
            mFetchInfo = new FetchInfo();
            mFetchInfo.execute(init);
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
    
    private void onScheduleSync(RainwaveResponse response) {
    	if(response == null) {
    	    // TODO: Some error here.
    	    return;
    	}
    	
    	updateSongInfo(response.getCurrentSong());
    	setRatings(response.getCurrentSong());
    }
    
    private void updateSongInfo(Song current) {
    	((TextView) findViewById(R.id.np_songTitle)).setText(current.song_title);
    	((TextView) findViewById(R.id.np_albumTitle)).setText(current.album_name);
    	((TextView) findViewById(R.id.np_artist)).setText(current.collapseArtists());
    }
    
    private void setRatings(Song current) {
    	((TextView) findViewById(R.id.np_songRating))
    	   .setText(getRatingString(current.song_rating_user, current.song_rating_avg));
    	
    	((TextView) findViewById(R.id.np_albumRating))
 	       .setText(getRatingString(current.album_rating_user, current.album_rating_avg));
    }
    
    private void onRateSong(RatingResult result) {
        mOrganizer.updateSongRatings(result);
        setRatings(mOrganizer.getCurrentSong());
    }
    
    private String getRatingString(float user, float avg) {
    	StringBuilder sb = new StringBuilder();
    	sb.append((user >= 1.0f) ? String.format("%1.1f", user) : "--");
    	sb.append("/");
    	sb.append((user >= 1.0f) ? String.format("%1.1f", avg) : "--");
    	return sb.toString();
    }
    
    private void updateAlbumArt(Bitmap art) {
        if(art == null) {
            // TODO: Some error here.
            art = BitmapFactory.decodeResource(getResources(), R.drawable.noart);
        }
        
        ((ImageView) findViewById(R.id.np_albumArt)).setImageBitmap(art);
    }
    
    protected class RateTask extends AsyncTask<Object, Integer, RatingResult> {
		@Override
		protected RatingResult doInBackground(Object ... params) {
			Log.d(TAG, "Submitting a rating...");
			int songId = (Integer) params[0];
			float rating = (Float) params[1];
			try {
				return mSession.rateSong(songId, rating);
			} catch (IOException e) {
				Log.e(TAG, "IO error: " + e.getMessage());
                // TODO: Show user error.
			} catch (RainwaveException e) {
				Log.e(TAG, "API error: " + e.getMessage());
				// TODO: Show user error.
			}
			return null;
		}
		
		protected void onPostExecute(RatingResult result) {
			Log.d(TAG, "Rating task ended.");
			mRateTask = null;
			if(result == null) return;
			onRateSong(result);
		}
    }
    
    /**
     * Fetches the now playing info.
     * @author pkilgo
     *
     */
    protected class FetchInfo extends AsyncTask<Boolean, Integer, Bundle> {
        private String TAG = "Unnamed";
        private boolean mInit = false;

        @Override
        protected Bundle doInBackground(Boolean ... flags) {
            mInit = flags[0];
            TAG = (mInit) ? "InitialPoll" : "UpdatePoll";
        	Log.d(TAG, "Fetching a schedule");
        	
            Bundle b = new Bundle();
            try {
                RainwaveResponse organizer =
                        (mInit)
                        	? (mSession.isAuthenticated())
                        			? mSession.syncInit()
                        			: mSession.asyncGet()
                        	: mSession.syncGet();
                        
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
            
            onScheduleSync(mOrganizer);
            updateAlbumArt( (Bitmap) result.getParcelable(ART) );
            
            if(mSession.isAuthenticated()) {
                syncSchedules();
            }
            
            Log.d(TAG, "Exiting successfully.");
        }
    }
    
    /** Dialog identifiers */
    public static final int
    	DIALOG_RATE = 0x4A7E;
    
    /** Bundle constants */
    public static final String
        SCHEDULE = "schedule",
        ART = "art";
}
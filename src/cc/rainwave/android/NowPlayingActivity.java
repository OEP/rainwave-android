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
import android.content.res.Resources;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * This is the primary activity for this application. It announces
 * which song is playing, handles ratings, and also elections.
 * @author pkilgo
 *
 */
public class NowPlayingActivity extends Activity {
    /** Debug tag */
	private static final String TAG = "NowPlaying";
	
	/** This is the last response from the last schedule sync */
	private RainwaveResponse mOrganizer;
	
	/** This manages our connection with the Rainwave server */
	private Session mSession;
	
	/** AsyncTask for schedule syncs */
	private FetchInfo mFetchInfo;
	
	/** AsyncTask for song ratings */
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
    
    /**
     * Our strategy here is to attempt to re-initialize the
     * app as much as possible. This helps us to catch preference
     * changes, and to not have lingering song data lying around.
     */
    @Override
    public void onResume() {
        super.onResume();
        initializeSession();
        initSchedules();
    }

    /**
     * We also want to stop our threads as much as possible, as they
     * should solely run in the foreground.
     */
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

    /**
     * Dialog manufacturer.
     */
    public Dialog onCreateDialog(int id) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	switch(id) {
    	    
    	// The 'rate song' dialog.
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
    	    // Assume the number must be a string resource id.
    		return builder.setTitle(R.string.label_error)
    				.setMessage(id)
    				.setPositiveButton(R.string.label_ok, null)
    				.create();
    	}
    }
    
    /**
     * Sets up listeners for this activity.
     */
    private void setListeners() {
    	// The rating dialog should show up if the Song rating view is clicked.
    	findViewById(R.id.np_songRating).setOnTouchListener(
    	new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				switch(e.getAction()) {
				case MotionEvent.ACTION_DOWN:
				    if(mOrganizer.isTunedIn() && mSession.isAuthenticated()) {
    					showDialog(DIALOG_RATE);
    					return true;
				    }
				    else {
				    	showDialog(R.string.msg_tunedInRate);
				    	return true;
				    }
				}
				return false;
			}
    	});
    	

    	final ListView election = (ListView) findViewById(R.id.np_electionList);
    	election.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    		public void onItemClick(AdapterView parent, View v, int i, long id) {
    			if(mOrganizer.isTunedIn() && mSession.isAuthenticated()) {
    				((ElectionListAdapter) election.getAdapter()).startCountdown(i);
    			}
    			else {
    				showDialog(R.string.msg_tunedInVote);
    			}
    		}
		});
    }
    
    /**
     * Stops ALL AsyncTasks and removes
     * all references to them.
     */
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
    
    /**
     * Performs an initial (e.g., non-longpoll) fetch
     * of our song info.
     */
    private void initSchedules() {
        fetchSchedules(true);
    }
    
    /**
     * Performs a long-polling synchronous update
     * of our song info.
     */
    private void syncSchedules() {
        fetchSchedules(false);
    }
    
    /**
     * Performs an update of song info.
     * @param init flag to indicate this
     *   is an initial (non-long-poll) fetch.
     */
    private void fetchSchedules(boolean init) {
        // Some really bad thing happened and we don't
        // have a connection at all.
        if(mSession == null) {
        	Rainwave.showError(NowPlayingActivity.this, R.string.msg_sessionError);
            return;
        }
        
        if(mFetchInfo == null) {
            mFetchInfo = new FetchInfo();
            mFetchInfo.execute(init);
        }
    }
    
    /** Shows the menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/** Responds to menu selection */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		    
		// Start RainwavePreferenceActivity.
		case R.id.menu_preferences:
			Intent i = new Intent(this, RainwavePreferenceActivity.class);
			startActivity(i);
			break;
		}
		
		return false;
	}
    
	/**
	 * Destroys any existing Session and creates
	 * a new Session object for us to use, pulling
	 * the user_id and key attributes from the default
	 * Preference store.
	 */
    private void initializeSession() {
        try {
            mSession = Session.makeSession(this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Executes when a schedule sync finished.
     * @param response the response the server issued
     */
    private void onScheduleSync(RainwaveResponse response) {
    	// Updates title, album, and artists.
    	updateSongInfo(response.getCurrentSong());
    	
    	// Updates song, album ratings.
    	setRatings(response.getCurrentSong());
    	
    	// Updates election info.
    	updateElection(response.getElection());
    	
    	// Updates tuned in state.
    	updateTunedIn(response);
    }
    
    private void updateTunedIn(RainwaveResponse response) {
    	Resources r = getResources();
    	String app_name = r.getString(R.string.app_name);
    	String state = r.getString(R.string.label_nottunedin);
    	if(response.isTunedIn()) {
    		state = r.getString(R.string.label_tunedin);
    	}
    	setTitle(String.format("%s (%s)", app_name, state));
    }
    
    private void updateElection(Song newSongs[]) {
    	((ListView)findViewById(R.id.np_electionList))
    	   .setAdapter(new ElectionListAdapter(this,mSession,newSongs));
    }
    
    /**
     * Updates the song title, album title, and
     * artists in the user interface.
     * @param current the current song that's playing.
     */
    private void updateSongInfo(Song current) {
    	((TextView) findViewById(R.id.np_songTitle)).setText(current.song_title);
    	((TextView) findViewById(R.id.np_albumTitle)).setText(current.album_name);
    	((TextView) findViewById(R.id.np_artist)).setText(current.collapseArtists());
    }
    
    /**
     * Updates the song and album ratings.
     * @param current the current song playing
     */
    private void setRatings(Song current) {
    	((CountdownView) findViewById(R.id.np_songRating))
    	   .setBoth(current.song_rating_user, current.song_rating_avg);
    	
    	((CountdownView) findViewById(R.id.np_albumRating))
 	       .setBoth(current.album_rating_user, current.album_rating_avg);
    }
    
    /**
     * Executes when a "rate song" request has finished.
     * @param result the result the server issued
     */
    private void onRateSong(RatingResult result) {
        mOrganizer.updateSongRatings(result);
        setRatings(mOrganizer.getCurrentSong());
    }
    
    /**
     * Sets the album art to the provided Bitmap, or
     * a default image if art is null.
     * @param art desired album art
     */
    private void updateAlbumArt(Bitmap art) {
        if(art == null) {
            Log.e(TAG, "Error fetching album art.");
            Rainwave.showError(this, R.string.msg_albumArtError);
            art = BitmapFactory.decodeResource(getResources(), R.drawable.noart);
        }
        
        ((ImageView) findViewById(R.id.np_albumArt)).setImageBitmap(art);
    }
    
    /**
     * AsyncTask for submitting a rating for a song.
     * Expects two arguments to <code>execute(Object...params)</code>,
     * which are song_id (int), and rating (float).
     * @author pkilgo
     *
     */
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
                Rainwave.showError(NowPlayingActivity.this, e);
			} catch (RainwaveException e) {
				Log.e(TAG, "API error: " + e.getMessage());
				Rainwave.showError(NowPlayingActivity.this, e);
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
     * Expects one argument to <code>execute(Object...params)</code> which
     * is the flag to indicate if this is an initializing (e.g., non-longpoll)
     * fetch of the schedule data.
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
                        	: mSession.syncGet(mOrganizer);
                        
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
            
            // Callback for schedule sync.
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
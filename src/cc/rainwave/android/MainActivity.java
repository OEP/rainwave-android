package cc.rainwave.android;

import java.io.IOException;

import cc.rainwave.android.MainActivity.FetchInfo;
import cc.rainwave.android.NowPlayingFragment.SongCountdownTask;
import cc.rainwave.android.adapters.SongListAdapter;
import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.RainwaveException;
import cc.rainwave.android.api.types.RainwaveResponse;
import cc.rainwave.android.api.types.Song;
import cc.rainwave.android.api.types.Station;

import com.android.music.TouchInterceptor;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.SlidingDrawer;

public class MainActivity extends FragmentActivity {

	private MainFragmentAdapter mAdapter;

	private ViewPager mPager;

	private Session mSession;

	private FetchInfo mFetchInfo;

	private RainwaveResponse mOrganizer;

	private  mSongCountdownTask;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setup();
		setContentView(R.layout.activity_main);

		mAdapter = new MainFragmentAdapter(getSupportFragmentManager());

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
	}

	public boolean onKeyDown(int keyCode, KeyEvent ev) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.np_drawer);
			if (drawer.isOpened()) {
				drawer.animateClose();
				return true;
			} else if (drawer.isMoving()) {
				drawer.close();
				return true;
			}
		}
		return super.onKeyDown(keyCode, ev);
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
		Intent i;
		switch (item.getItemId()) {

		// Start RainwavePreferenceActivity.
		case R.id.menu_preferences:
			startPreferences();
			break;

		case R.id.menu_refresh:
			// refresh();
			break;
		}

		return false;
	}

	private void startPreferences() {
		Intent i = new Intent(this, RainwavePreferenceActivity.class);
		startActivity(i);
	}

	private void setup() {
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		Rainwave.forceCompatibility(this);
	}
	
    /**
     * Starts AsyncTask for song countdown.
     * @param endTime the UTC time to stop counting
     */
    private void startCountdown(long endTime) {
    	if(mSongCountdownTask != null) {
    		mSongCountdownTask.cancel(true);
    	}
    	
    	mSongCountdownTask = new SongCountdownTask();
    	mSongCountdownTask.execute(endTime);
    }

	/**
	 * Performs an update of song info.
	 * 
	 * @param init
	 *            flag to indicate this is an initial (non-long-poll) fetch.
	 */
	private void fetchSchedules(boolean init) {
		// Some really bad thing happened and we don't
		// have a connection at all.
		if (mSession == null) {
			Rainwave.showError(this, R.string.msg_sessionError);
			return;
		}

		if (mFetchInfo == null) {
			mFetchInfo = new FetchInfo();
			mFetchInfo.execute(init);
		}
	}

	private void stopTasks() {
		if (mFetchInfo != null) {
			mFetchInfo.cancel(true);
			mFetchInfo = null;
		}
	}

	/**
	 * Performs an initial (e.g., non-longpoll) fetch of our song info.
	 */
	private void initSchedules() {
		fetchSchedules(true);
	}

	/**
	 * Performs a long-polling synchronous update of our song info.
	 */
	private void syncSchedules() {
		fetchSchedules(false);
	}

	/**
	 * Stops all running tasks and re-initializes the schedule.
	 */
	public void refresh() {
		stopTasks();
		fetchSchedules(true);
	}

	private void dispatchThrobberVisibility(boolean state) {
		Message msg = mHandler.obtainMessage(HANDLER_SET_INDETERMINATE);
		Bundle data = msg.getData();
		data.putBoolean(BOOL_STATUS, state);
		msg.sendToTarget();
	}

	private void dispatchTitleUpdate(String title) {
		Message msg = mHandler.obtainMessage(UPDATE_TITLE);
		Bundle data = msg.getData();
		data.putString(STRING_TITLE, title);
		msg.sendToTarget();
	}

	/**
	 * Destroys any existing Session and creates a new Session object for us to
	 * use, pulling the user_id and key attributes from the default Preference
	 * store.
	 */
	private void initializeSession() {
		try {
			// TODO: Maybe ASK the user before we override?
			handleIntent(getIntent());
			mSession = Session.makeSession(this);
		} catch (IOException e) {
			Rainwave.showError(this, e);
		}
	}

	/**
	 * Handles stuff included from the intent. Called once each time a Session
	 * is initialized.
	 * 
	 * @param i
	 *            the intent to handle
	 * @return true if no error occurred, false if there was some error handling
	 *         the URL.
	 */
	private boolean handleIntent(Intent i) {
		if (i == null) {
			return true;
		}
		Bundle b = i.getExtras();
		Uri uri = i.getData();

		// No uri? No need to handle.
		if (uri == null) {
			return true;
		}

		boolean handled = (b != null)
				&& b.getBoolean(Rainwave.HANDLED_URI, false);

		if (handled) {
			return true;
		}

		i.putExtra(Rainwave.HANDLED_URI, true);
		boolean ok = Rainwave.setPreferencesFromUri(this, uri);

		if (!ok) {
			Rainwave.showError(this, R.string.msg_invalidUrl);
		}

		return ok;
	}
	
	/**
	 * Sets the vote drawer to opened or closed.
	 * 
	 * @param state
	 *            , true for open, false for closed
	 */
	private void setDrawerState(boolean state) {
		boolean pref = Rainwave.getAutoShowElectionFlag(getActivity());
		if (!pref)
			return;
		SlidingDrawer v = (SlidingDrawer) getView()
				.findViewById(R.id.np_drawer);
		if (state && !v.isOpened()) {
			v.animateOpen();
		} else if (v.isOpened()) {
			v.animateClose();
		}
	}
	
    private void updateTimer(int seconds) {
    	updateTitle(mOrganizer, seconds);
    }
    
    private void updateTunedIn(RainwaveResponse response) {
    	long end = response.getEndTime();
    	long utc = System.currentTimeMillis() / 1000;
    	updateTitle(response, (int) (end - utc));
    }
    
    private void updateTitle(RainwaveResponse response, int seconds) {
    	seconds = Math.max(0, seconds);
    	int minutes = seconds / 60;
    	seconds %= 60;
    	
    	Resources r = getResources();
    	int id = mSession.getStationId();
    	String stationName = mOrganizer.getStationName(id);
    	String title = (stationName != null) ? stationName : r.getString(R.string.app_name);
    	String state = r.getString(R.string.label_nottunedin);
    	
    	if(!mSession.isAuthenticated()) {
    		state = r.getString(R.string.label_anonymous);
    	}
    	else if(response.isTunedIn()) {
    		state = r.getString(R.string.label_tunedin);
    	}
    	
    	// Update thread-safe since this method may be called by AsyncTask.
    	dispatchTitleUpdate(String.format("[%d:%02d] %s (%s)", minutes, seconds, title, state));
    }
	
    protected class SongCountdownTask extends AsyncTask<Long, Integer, Boolean> {
        private String TAG = "Unnamed";

        @Override
        protected Boolean doInBackground(Long ... params) {
        	long stopTime = params[0];

        	long utc = System.currentTimeMillis() / 1000;
        	
        	while(utc < stopTime) {
        		try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return false;
				}
        		utc = System.currentTimeMillis() / 1000;
        		updateTimer((int) (stopTime - utc));
        	}
        	return true;
        }
        
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }

	/**
	 * Fetches the now playing info. Expects one argument to
	 * <code>execute(Object...params)</code> which is the flag to indicate if
	 * this is an initializing (e.g., non-longpoll) fetch of the schedule data.
	 * 
	 * @author pkilgo
	 * 
	 */
	protected class FetchInfo extends AsyncTask<Boolean, Integer, Bundle> {
		private String TAG = "Unnamed";
		private boolean mInit = false;

		@Override
		protected Bundle doInBackground(Boolean... flags) {
			mInit = flags[0];

			if (mInit) {
				dispatchThrobberVisibility(true);
			}

			TAG = (mInit) ? "InitialPoll" : "UpdatePoll";
			Log.d(TAG, "Fetching a schedule");

			Bundle b = new Bundle();
			try {
				RainwaveResponse organizer = (mInit) ? (mSession
						.isAuthenticated()) ? mSession.syncInit() : mSession
						.asyncGet() : mSession.syncGet(mOrganizer);

				if (mInit) {
					Station stations[] = mSession.getStations();
					organizer.setStations(stations);
				}

				b.putParcelable(Rainwave.SCHEDULE, organizer);

				if (!organizer.hasError()) {
					Song song = organizer.getCurrentSong();
					Bitmap art = mSession.fetchAlbumArt(song.album_art);
					b.putParcelable(Rainwave.ART, art);
				}

				return b;
			} catch (IOException e) {
				Log.e(TAG, "IOException occured: " + e);
				Rainwave.showError(MainActivity.this, e);
				return null;
			} catch (RainwaveException e) {
				Log.e(TAG, "API error: " + e.getMessage());
				Rainwave.showError(MainActivity.this, e);
				return null;
			}

		}

		protected void onPostExecute(Bundle result) {
			super.onPostExecute(result);

			dispatchThrobberVisibility(false);

			mFetchInfo = null;

			// Was there an IO failure?
			if (result == null) {
				mFetchInfo = null;
				return;
			}

			if (mOrganizer == null) {
				mOrganizer = result.getParcelable(Rainwave.SCHEDULE);
			} else {
				RainwaveResponse tmp = result.getParcelable(Rainwave.SCHEDULE);
				mOrganizer.receiveUpdates(tmp);
			}

			// Callback for schedule sync.
			onScheduleSync(mOrganizer);
			updateAlbumArt((Bitmap) result.getParcelable(Rainwave.ART));

			if (mSession.isAuthenticated()) {
				syncSchedules();
			}

			startCountdown(mOrganizer.getEndTime());

			Log.d(TAG, "Exiting successfully.");
		}
	}

	public static class MainFragmentAdapter extends FragmentPagerAdapter {
		public MainFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
			case 1:
			default:
				return NowPlayingFragment.newInstance(null);
			}
		}

		public static final int NUM_ITEMS = 2;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Bundle data = msg.getData();
			switch (msg.what) {
			case HANDLER_SET_INDETERMINATE:
				setProgressBarIndeterminateVisibility(data
						.getBoolean(BOOL_STATUS));
				break;

			case UPDATE_TITLE:
				setTitle(data.getString(STRING_TITLE));
				break;
				
			case SongListAdapter.CODE_VOTED:
				if (msg.arg1 == SongListAdapter.CODE_SUCCESS) {
					setDrawerState(false);
				}
				break;
			}
		}
	};

	/** Handler keys */
	private static final String BOOL_STATUS = "bool_status";

	/** Handler keys */
	private static final String STRING_TITLE = "string_title";

	/** Handler codes */
	private static final int UPDATE_TITLE = 0x71713,
			HANDLER_SET_INDETERMINATE = 0x1D373;
}

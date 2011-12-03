package cc.rainwave.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import cc.rainwave.android.adapters.SongListAdapter;
import cc.rainwave.android.adapters.StationListAdapter;
import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.GenericResult;
import cc.rainwave.android.api.types.RainwaveException;
import cc.rainwave.android.api.types.RainwaveResponse;
import cc.rainwave.android.api.types.Song;
import cc.rainwave.android.api.types.Station;
import cc.rainwave.android.tasks.ActionTask;
import cc.rainwave.android.views.HorizontalRatingBar;
import cc.rainwave.android.views.PagerWidget;

import com.android.music.TouchInterceptor;

/**
 * This is the primary activity for this application. It announces which song is
 * playing, handles ratings, and also elections.
 * 
 * @author pkilgo
 * 
 */
public class NowPlayingFragment extends Fragment {
	/** Debug tag */
	private static final String TAG = "NowPlaying";

	/** This is the last response from the last schedule sync */
	private RainwaveResponse mOrganizer;

	/** This manages our connection with the Rainwave server */
	private Session mSession;

	/** AsyncTask for song ratings */
	private ActionTask mRateTask;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeSession();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	/**
	 * Our strategy here is to attempt to re-initialize the app as much as
	 * possible. This helps us to catch preference changes, and to not have
	 * lingering song data lying around.
	 */
	@Override
	public void onResume() {
		super.onResume();
		initializeSession();
		initSchedules();
	}

	/**
	 * We also want to stop our threads as much as possible, as they should
	 * solely run in the foreground.
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

	static NowPlayingFragment newInstance(RainwaveResponse res) {
		NowPlayingFragment f = new NowPlayingFragment();
		Bundle data = new Bundle();
		data.putParcelable("response", res);
		f.setArguments(data);
		return f;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		TouchInterceptor list = (TouchInterceptor) v;
		inflater.inflate(R.menu.queue_menu, menu);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Song s = (Song) list.getItemAtPosition(info.position);
		menu.setHeaderTitle(s.song_title);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.remove:
			TouchInterceptor list = (TouchInterceptor) getView().findViewById(
					R.id.np_request_list);
			SongListAdapter adapter = (SongListAdapter) list.getAdapter();
			Song s = adapter.removeSong(info.position);
			requestRemove(s);
			resyncRequests();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * Sets up listeners for this activity.
	 */
	private void setListeners() {
		// The rating dialog should show up if the Song rating view is clicked.
		getView().findViewById(R.id.np_songRating).setOnTouchListener(
				new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent e) {
						HorizontalRatingBar b = (HorizontalRatingBar) getView()
								.findViewById(R.id.np_songRating);

						if (mOrganizer == null || !mOrganizer.isTunedIn()
								|| !mSession.isAuthenticated()) {
							if (e.getAction() == MotionEvent.ACTION_DOWN) {
								b.setLabel(R.string.msg_tuneInFirst);
							} else if (e.getAction() == MotionEvent.ACTION_UP) {
								b.setLabel(R.string.label_song);
							}
							return true;
						}

						HorizontalRatingBar hrb = (HorizontalRatingBar) v;
						float rating = 0.0f;
						float max = 5.0f;
						switch (e.getAction()) {
						case MotionEvent.ACTION_DOWN:
						case MotionEvent.ACTION_MOVE:
						case MotionEvent.ACTION_UP:
							rating = hrb.snapPositionToMinorIncrement(e.getX());
							rating = Math.max(1.0f, Math.min(rating, 5.0f));
							max = hrb.getMax();
							hrb.setPrimaryValue(rating);
							String label = String.format("%.1f/%.1f", rating,
									max);
							hrb.setLabel(label);

							if (e.getAction() == MotionEvent.ACTION_UP) {
								ActionTask t = new ActionTask(getActivity());
								Song s = mOrganizer.getCurrentSong();
								t.execute(ActionTask.RATE, s.song_id, rating);
								b.setLabel(R.string.label_song);
							}
						}
						return true;
					}
				});

		getView().findViewById(R.id.np_albumRating).setOnTouchListener(
				new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent e) {
						HorizontalRatingBar b = (HorizontalRatingBar) getView()
								.findViewById(R.id.np_albumRating);

						if (mOrganizer == null || !mOrganizer.isTunedIn()
								|| !mSession.isAuthenticated()) {
							if (e.getAction() == MotionEvent.ACTION_DOWN) {
								b.setLabel(R.string.msg_tuneInFirst);
							} else if (e.getAction() == MotionEvent.ACTION_UP) {
								b.setLabel(R.string.label_album);
							}
							return true;
						}

						HorizontalRatingBar hrb = (HorizontalRatingBar) v;
						float rating = 0.0f;
						float max = 5.0f;
						switch (e.getAction()) {
						case MotionEvent.ACTION_DOWN:
						case MotionEvent.ACTION_MOVE:
						case MotionEvent.ACTION_UP:
							rating = hrb.getPrimary();
							max = hrb.getMax();
							String label = String.format("%.1f/%.1f", rating,
									max);
							hrb.setLabel(label);

							if (e.getAction() == MotionEvent.ACTION_UP) {
								b.setLabel(R.string.label_album);
							}
						}
						return true;
					}
				});

		final ListView election = (ListView) getView().findViewById(
				R.id.np_electionList);
		election.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v, int i, long id) {
				if (mOrganizer.isTunedIn() && mSession.isAuthenticated()) {
					((SongListAdapter) election.getAdapter()).startCountdown(i);
				} else {
					Log.w("REMOVE", "Tuned in to vote message!");
				}
			}
		});

		final TouchInterceptor requestList = ((TouchInterceptor) getView()
				.findViewById(R.id.np_request_list));
		requestList.setDropListener(new TouchInterceptor.DropListener() {
			@Override
			public void drop(int from, int to) {
				if (from == to)
					return;
				SongListAdapter adapter = (SongListAdapter) requestList
						.getAdapter();
				adapter.moveSong(from, to);

				ArrayList<Song> songs = adapter.getSongs();
				requestReorder(songs.toArray(new Song[songs.size()]));
			}
		});

		// Button Listeners.
		ImageButton play = (ImageButton) getView().findViewById(R.id.np_play);
		ImageButton station = (ImageButton) getView().findViewById(
				R.id.np_stationPick);
		ImageButton request = (ImageButton) getView().findViewById(
				R.id.np_makeRequest);

		play.setEnabled(false);
		station.setEnabled(false);

		play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startPlayer();
			}
		});

		station.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().showDialog(DIALOG_STATION_PICKER);
			}
		});

		request.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startPlaylist();
			}
		});

		registerForContextMenu(getView().findViewById(R.id.np_request_list));
	}

	private void startPlaylist() {
		Intent i = new Intent(getActivity(), PlaylistActivity.class);
		startActivity(i);
	}

	/**
	 * Stops ALL AsyncTasks and removes all references to them.
	 */
	private void stopTasks() {
		if (mRateTask != null) {
			mRateTask.cancel(true);
			mRateTask = null;
		}

		if (mSongCountdownTask != null) {
			mSongCountdownTask.cancel(true);
			mSongCountdownTask = null;
		}
	}

	private void requestReorder(Song requests[]) {
		if (mSession == null) {
			Rainwave.showError(getActivity(), R.string.msg_sessionError);
			return;
		}

		if (mReorderTask == null) {
			mReorderTask = new ActionTask();
			mReorderTask.execute(ActionTask.REORDER, requests);
		}
	}

	private void requestRemove(Song s) {
		if (mSession == null) {
			Rainwave.showError(getActivity(), R.string.msg_sessionError);
			return;
		}

		if (mRemoveTask == null) {
			mRemoveTask = new ActionTask();
			mRemoveTask.execute(ActionTask.REMOVE, s);
		}
	}

	private void startPlayer() {
		int stationId = mSession.getStationId();
		Station s = mOrganizer.getStation(stationId);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse(s.stream), "audio/*");
		startActivity(i);
	}

	/**
	 * Destroys any existing Session and creates a new Session object for us to
	 * use, pulling the user_id and key attributes from the default Preference
	 * store.
	 */
	private void initializeSession() {
		try {
			// TODO: Maybe ASK the user before we override?
			handleIntent(getActivity().getIntent());
			mSession = Session.makeSession(getActivity());
		} catch (IOException e) {
			Rainwave.showError(getActivity(), e);
		}

		if (getView() == null)
			return;
		View playlistButton = getView().findViewById(R.id.np_makeRequest);
		if (playlistButton != null) {
			playlistButton.setVisibility((mSession != null && mSession
					.isAuthenticated()) ? View.VISIBLE : View.GONE);
		}
	}

	/**
	 * Executes when a schedule sync finished.
	 * 
	 * @param response
	 *            the response the server issued
	 */
	public void onScheduleSync(RainwaveResponse response) {
		// We should enable the buttons now.
		ImageButton play = (ImageButton) getView().findViewById(R.id.np_play);
		ImageButton station = (ImageButton) getView().findViewById(
				R.id.np_stationPick);

		play.setEnabled(true);
		station.setEnabled(true);

		// Updates title, album, and artists.
		updateSongInfo(response.getCurrentSong());

		// Updates song, album ratings.
		setRatings(response.getCurrentSong());

		// Updates election info.
		updateElection(response);

		// Updates request lsit.
		updateRequests(response);
	}

	private void updateElection(RainwaveResponse response) {
		SongListAdapter adapter = new SongListAdapter(getActivity(),
				R.layout.item_song_election, mSession, new ArrayList<Song>(
						Arrays.asList(response.getElection())));
		((ListView) getView().findViewById(R.id.np_electionList))
				.setAdapter(adapter);

		// Set vote deadline for when the song ends.
		adapter.setDeadline(response.getEndTime());

		// Open the drawer if the user can vote.
		boolean canVote = !response.hasVoteResult() && response.isTunedIn();
		setDrawerState(canVote);

		// Set the vote listener for th list adapter.
		adapter.setOnVoteHandler(mHandler);

		if (response.hasVoteResult()) {
			adapter.markVoted(response.getPastVote());
		}
	}

	private void updateRequests(RainwaveResponse response) {
		if (response == null) {
			resyncRequests();
			return;
		}

		TouchInterceptor requestList = (TouchInterceptor) getView()
				.findViewById(R.id.np_request_list);
		Song songs[] = response.getRequests();

		requestList.setAdapter(new SongListAdapter(getActivity(),
				R.layout.item_song_request, mSession,
				(songs != null) ? new ArrayList<Song>(Arrays.asList(songs))
						: new ArrayList<Song>()));

		resyncRequests();
	}

	private void resyncRequests() {
		TouchInterceptor requestList = (TouchInterceptor) getView()
				.findViewById(R.id.np_request_list);
		SongListAdapter adapter = (SongListAdapter) requestList.getAdapter();
		int visibility = (adapter.getCount()) > 0 ? View.GONE : View.VISIBLE;
		getView().findViewById(R.id.np_request_overlay).setVisibility(
				visibility);
	}

	/**
	 * Updates the song title, album title, and artists in the user interface.
	 * 
	 * @param current
	 *            the current song that's playing.
	 */
	private void updateSongInfo(Song current) {
		((TextView) getView().findViewById(R.id.np_songTitle))
				.setText(current.song_title);
		((TextView) getView().findViewById(R.id.np_albumTitle))
				.setText(current.album_name);
		((TextView) getView().findViewById(R.id.np_artist)).setText(current
				.collapseArtists());

		ImageView accent = (ImageView) getView().findViewById(R.id.np_accent);
		TextView requestor = (TextView) getView().findViewById(
				R.id.np_requestor);
		Resources r = getResources();

		if (current.isRequest()) {
			accent.setImageResource(R.drawable.accent_song_hilight);
			requestor.setVisibility(View.VISIBLE);
			requestor.setText(String.format(
					r.getString(R.string.label_requestor),
					current.song_requestor));
		} else {
			accent.setImageResource(R.drawable.accent_song);
			requestor.setVisibility(View.GONE);
		}
	}

	/**
	 * Updates the song and album ratings.
	 * 
	 * @param current
	 *            the current song playing
	 */
	private void setRatings(Song current) {
		((HorizontalRatingBar) getView().findViewById(R.id.np_songRating))
				.setBothValues(current.song_rating_user,
						current.song_rating_avg);

		((HorizontalRatingBar) getView().findViewById(R.id.np_albumRating))
				.setBothValues(current.album_rating_user,
						current.album_rating_avg);
	}

	/**
	 * Executes when a "rate song" request has finished.
	 * 
	 * @param result
	 *            the result the server issued
	 */
	private void onRateSong(GenericResult result) {
		mOrganizer.updateSongRatings(result);
		setRatings(mOrganizer.getCurrentSong());
	}

	/**
	 * Sets the album art to the provided Bitmap, or a default image if art is
	 * null.
	 * 
	 * @param art
	 *            desired album art
	 */
	private void updateAlbumArt(Bitmap art) {
		if (art == null) {
			Log.e(TAG, "Error fetching album art.");
			Rainwave.showError(getActivity(), R.string.msg_albumArtError);
			art = BitmapFactory
					.decodeResource(getResources(), R.drawable.noart);
		}

		((ImageView) getView().findViewById(R.id.np_albumArt))
				.setImageBitmap(art);
	}

	/** Dialog identifiers */
	public static final int DIALOG_STATION_PICKER = 0xb1c7;
}
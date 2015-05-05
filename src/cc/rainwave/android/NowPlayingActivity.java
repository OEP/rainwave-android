/*
 * Copyright (c) 2013, Paul M. Kilgo
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of Paul Kilgo nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cc.rainwave.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import cc.rainwave.android.adapters.SongListAdapter;
import cc.rainwave.android.adapters.StationListAdapter;
import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.Album;
import cc.rainwave.android.api.types.RainwaveException;
import cc.rainwave.android.api.types.Song;
import cc.rainwave.android.api.types.SongRating;
import cc.rainwave.android.api.types.Station;
import cc.rainwave.android.views.HorizontalRatingBar;
import cc.rainwave.android.views.PagerWidget;

import com.android.music.TouchInterceptor;
import com.google.android.apps.iosched.ui.widget.Workspace;
import com.google.android.apps.iosched.ui.widget.Workspace.OnScreenChangeListener;

/**
 * This is the primary activity for this application. It announces
 * which song is playing, handles ratings, and also elections.
 */
public class NowPlayingActivity extends Activity {
    /** Debug tag */
    private static final String TAG = "NowPlaying";

    /** This manages our connection with the Rainwave server */
    private Session mSession;
    private RainwavePreferences mPreferences;

    /** Reference to song countdown timer. */
    private CountDownTimer mCountdown;

    /** True if device supports Window.FEATURE_INDETERMINATE_PROGRESS. */
    private boolean mHasIndeterminateProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mHasIndeterminateProgress = requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        initializeSession();
        setContentView(R.layout.activity_main);
        setListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        // Assuming a cold start every time, so need to register the schedule
        // update receiver and update the schedule if needed.
        super.onResume();
        initializeSession();

        // Construct intent filter
        IntentFilter filter = new IntentFilter();
        filter.addAction(SyncService.BROADCAST_EVENT_UPDATE);
        filter.addAction(SyncService.BROADCAST_EVENT_UPDATE_FAILED);
        filter.addAction(SyncService.BROADCAST_REAUTHENTICATE);
        registerReceiver(mEventUpdateReceiver, filter);

        fetchSchedules();
    }

    public void onPause() {
        // Should not continue any long-running background tasks when not in
        // the foreground.
        super.onPause();
        if(mCountdown != null) {
            mCountdown.cancel();
        }
        stopSyncService();
        unregisterReceiver(mEventUpdateReceiver);
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    public boolean onKeyDown(int keyCode, KeyEvent ev) {
        switch(keyCode) {
        case KeyEvent.KEYCODE_BACK:
            SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.np_drawer);
            if(drawer.isOpened()) {
                drawer.animateClose();
                return true;
            }
            else if(drawer.isMoving()) {
                drawer.close();
                return true;
            }
        }
        return super.onKeyDown(keyCode, ev);
    }

    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id) {

        case DIALOG_STATION_PICKER:
            builder.setTitle(R.string.label_pickStation)
                   .setNegativeButton(R.string.label_cancel, null);

            // Print a slightly helpful message if the stations were not
            // retrieved for some reason.
            if(!mSession.hasStations()) {
                return builder.setMessage(R.string.msg_noStations).create();
            }

            Station stations[] = mSession.cloneStations();

            // Change station when one is selected from dialog.
            final ListView listView = new ListView(this);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int index, long id) {
                    Station s = (Station) listView.getItemAtPosition(index);
                    mSession.setStation(s.getId());
                    NowPlayingActivity.this.dismissDialog(DIALOG_STATION_PICKER);
                    refresh();
                }
            });

            listView.setAdapter(new StationListAdapter(this, stations));

            return builder.setView(listView)
                .create();

        default:
            // Assume the number must be a string resource id.
            return builder.setTitle(R.string.label_error)
                    .setMessage(id)
                    .setPositiveButton(R.string.label_ok, null)
                    .create();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        TouchInterceptor list = (TouchInterceptor) findViewById(R.id.np_request_list);
        inflater.inflate(R.menu.queue_menu, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Song s = (Song) list.getItemAtPosition(info.position);
        menu.setHeaderTitle(s.getTitle());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.remove:
            TouchInterceptor list = (TouchInterceptor) findViewById(R.id.np_request_list);
            SongListAdapter adapter = (SongListAdapter) list.getAdapter();
            Song s = adapter.getItem(info.position);
            adapter.remove(s);
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
        findViewById(R.id.np_songRating).setOnTouchListener(
        new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                Workspace w = (Workspace) findViewById(R.id.np_workspace);
                HorizontalRatingBar b = (HorizontalRatingBar) findViewById(R.id.np_songRating);

                if(mSession == null || !mSession.isTunedIn() || !mSession.hasCredentials()) {
                    if(e.getAction() == MotionEvent.ACTION_DOWN) {
                        w.lockCurrentScreen();
                        b.setLabel(R.string.msg_tuneInFirst);
                    }
                    else if(e.getAction() == MotionEvent.ACTION_UP) {
                        w.unlockCurrentScreen();
                        b.setLabel(R.string.label_song);
                    }
                    return true;
                }


                HorizontalRatingBar hrb = (HorizontalRatingBar) v;
                float rating = 0.0f;
                float max = 5.0f;
                switch(e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    w.lockCurrentScreen();
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    rating = hrb.snapPositionToMinorIncrement(e.getX());
                    rating = Math.max(1.0f, Math.min(rating, 5.0f));
                    max = hrb.getMax();
                    hrb.setPrimaryValue(rating);
                    String label = String.format(Locale.US, "%.1f/%.1f",rating,max);
                    hrb.setLabel(label);

                    if(e.getAction() == MotionEvent.ACTION_UP) {
                        w.unlockCurrentScreen();
                        Song s = mSession.getCurrentEvent().getCurrentSong();
                        new RateTask(s, rating).execute();
                        b.setLabel(R.string.label_song);
                    }
                }
                return true;
            }
        });

        // Show album rating when the album rating bar is touched.
        findViewById(R.id.np_albumRating).setOnTouchListener(
        new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                Workspace w = (Workspace) findViewById(R.id.np_workspace);
                HorizontalRatingBar b = (HorizontalRatingBar) findViewById(R.id.np_albumRating);

                if(mSession == null || !mSession.isTunedIn() || !mSession.hasCredentials()) {
                    if(e.getAction() == MotionEvent.ACTION_DOWN) {
                        w.lockCurrentScreen();
                        b.setLabel(R.string.msg_tuneInFirst);
                    }
                    else if(e.getAction() == MotionEvent.ACTION_UP) {
                        w.unlockCurrentScreen();
                        b.setLabel(R.string.label_album);
                    }
                    return true;
                }


                HorizontalRatingBar hrb = (HorizontalRatingBar) v;
                float rating = 0.0f;
                float max = 5.0f;
                switch(e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    w.lockCurrentScreen();
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    rating = hrb.getPrimary();
                    max = hrb.getMax();
                    String label = String.format(Locale.US, "%.1f/%.1f",rating,max);
                    hrb.setLabel(label);

                    if(e.getAction() == MotionEvent.ACTION_UP) {
                        w.unlockCurrentScreen();
                        b.setLabel(R.string.label_album);
                    }
                }
                return true;
            }
        });


        // Spawn a vote task when an item from the election drawer is selected.
        final ListView election = (ListView) findViewById(R.id.np_electionList);
        election.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int i, long id) {
                SongListAdapter adapter = (SongListAdapter) election.getAdapter();
                Song song = adapter.getItem(i);

                // Do nothing if they selected the item we last voted for.
                if(song.getElectionEntryId() == mSession.getLastVoteId()) {
                    return;
                }

                // Show a message if they are not tuned in.
                if(mSession.isTunedIn() && mSession.hasCredentials()) {
                    new VoteTask(song).execute();
                }
                else {
                    showDialog(R.string.msg_tunedInVote);
                }
            }
        });

        // Reorder requests when the users drags-and-drops them.
        final TouchInterceptor requestList = ((TouchInterceptor) findViewById(R.id.np_request_list));
        requestList.setDropListener(new TouchInterceptor.DropListener() {
            @Override
            public void drop(int from, int to) {
                if(from == to) return;
                SongListAdapter adapter = (SongListAdapter) requestList.getAdapter();
                Song s = adapter.getItem(from);
                adapter.remove(s);
                adapter.insert(s, to);

                Song songs[] = new Song[adapter.getCount()];
                for(int i = 0; i < adapter.getCount(); i++) {
                    songs[i] = adapter.getItem(i);
                }
                requestReorder(songs);
            }
        });

        // Lock down the current workspace when clicking on the drag handles of
        // the request list.
        requestList.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                if(ev.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
                if(requestList.getCount() == 0) return false;
                Workspace w = (Workspace) findViewById(R.id.np_workspace);

                // Benefit of the doubt: unlock in case we locked earlier.
                if(ev.getAction() == MotionEvent.ACTION_UP) {
                    w.unlockCurrentScreen();
                }

                float x = ev.getX();
                if(ev.getAction() == MotionEvent.ACTION_DOWN && x < 64) {
                    w.lockCurrentScreen();
                }
                return false;
            }
        });

        // Update the pager when the workspace changes.
        Workspace w = (Workspace) findViewById(R.id.np_workspace);
        w.setOnScreenChangeListener(new OnScreenChangeListener() {
            @Override
            public void onScreenChanged(View newScreen, int newScreenIndex) {
                PagerWidget pw = (PagerWidget) findViewById(R.id.pager);
                pw.setCurrent(newScreenIndex);
            }

            @Override
            public void onScreenChanging(View newScreen, int newScreenIndex) {

            }
        });

        registerForContextMenu(findViewById(R.id.np_request_list));
    }

    /** Tell a SyncService that it should stop. */
    private void stopSyncService() {
        stopService(new Intent(NowPlayingActivity.this, SyncService.class)
                .setAction(SyncService.ACTION_INFO)
        );
        stopService(new Intent(NowPlayingActivity.this, SyncService.class)
                .setAction(SyncService.ACTION_SYNC)
        );
    }

    /**
     * Force an update of the schedule information.
     */
    private void refresh() {
        stopSyncService();
        Intent local = new Intent(this, SyncService.class);
        local.setAction(SyncService.ACTION_INFO);
        startService(local);
        if(mHasIndeterminateProgress) {
            setProgressBarIndeterminateVisibility(true);
            setProgressBarIndeterminate(true);
        }
    }

    /**
     * Performs an update of song info.
     * @param init flag to indicate this
     *   is an initial (non-long-poll) fetch.
     */
    private void fetchSchedules() {
        // Only do an immediate fetch from here.
        if(mSession.requiresSync()) {
            refresh();
        }
        else {
            // already have one so just sync
            onScheduleSync();
        }
    }

    /** Spawn a background task to send new request ordering. */
    private void requestReorder(Song requests[]) {
        new ReorderTask().execute(requests);
    }

    /** Spawn a new background task to remove a requested song. */
    private void requestRemove(Song s) {
        new RemoveTask(s).execute();
    }

    /**
     * Sets the vote drawer to opened or closed. Does nothing if the auto-slide preference is disabled.
     * 
     * @param state, true for open, false for closed
     */
    private void setDrawerState(boolean state) {
        if(!mPreferences.getAutoshowElection()) {
            return;
        }
        SlidingDrawer v = (SlidingDrawer) this.findViewById(R.id.np_drawer);
        if(state && !v.isOpened()) {
            v.animateOpen();
        }
        else if(v.isOpened()) {
            v.animateClose();
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

        // Start RainwavePreferenceActivity.
        case R.id.menu_preferences:
            startPreferences();
            break;

        case R.id.menu_refresh:
            refresh();
            break;

        case R.id.menu_playStream:
            startPlayer();
            break;

        case R.id.menu_playlist:
            startPlaylist();
            break;
            

        case R.id.menu_pickStation:
            showDialog(NowPlayingActivity.DIALOG_STATION_PICKER);
            break;
        }

        return false;
    }

    /** Starts the media player for the current station's stream. */
    private void startPlayer() {
        int stationId = mSession.getStationId();
        Station station = null;
        if(mSession.hasStations()) {
            station = mSession.getStation(stationId);
        }
        if(station != null) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse(station.getMainStream()), "audio/*");
            startActivity(i);    
        }
        else {
            Toast.makeText(this, R.string.msg_streamNotKnown, Toast.LENGTH_SHORT).show();
        }
    }

    /** Start the PlaylistActivity. */
    private void startPlaylist() {
        if(!mSession.hasCredentials()) {
            Toast.makeText(this, R.string.msg_authenticationRequired, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, PlaylistActivity.class);
        startActivity(i);
    }

    /** Start the PreferencesActivity. */
    private void startPreferences() {
        Intent i = new Intent(this, RainwavePreferenceActivity.class);
        startActivity(i);
    }

    /**
     * Destroys any existing Session and creates a new Session object for us to
     * use, pulling the user_id and key attributes from the default Preference
     * store.
     */
    private void initializeSession() {
        handleIntent();
        mSession = Session.getInstance(this);
        mSession.unpickle();
        mPreferences = RainwavePreferences.getInstance(this);
    }


    /**
     * Handle activity intent. This activity is configured to handle rw:// URL's
     * if triggered from elsewhere in the OS.
     */
    private void handleIntent() {
        final Intent i = getIntent();

        if(i == null) {
            return;
        }
        Bundle b = i.getExtras();
        Uri uri = i.getData();

        if(uri == null){
            return;
        }

        // check if this Intent was previously handled
        boolean handled = (b != null) && b.getBoolean("handled-uri", false);
        if(handled) {
            return;
        }

        // store in preferences if all is well
        final String parts[] = Utility.parseUrl(uri);
        if(parts != null) {
            mPreferences.setUserInfo(parts[0], parts[1]);
        }

        i.putExtra("handled-uri", true);
    }

    /**
     * Executes when a schedule sync finished.
     * @param response the response the server issued
     */
    private void onScheduleSync() {
        // Updates title, album, and artists.
        updateSongInfo(mSession.getCurrentEvent().getCurrentSong());

        // Updates song, album ratings.
        setRatings(mSession.getCurrentEvent().getCurrentSong());

        // Updates election info.
        updateElection();

        // Refresh clock and title bar state.
        refreshTitle();

        // Updates request lsit.
        updateRequests();

        // Update album art if there is any
        updateAlbumArt(mSession.getCurrentAlbumArt());

        // Cancel old countdown if there is one.
        if(mCountdown != null) {
            mCountdown.cancel();
        }

        // Start a countdown task for the event.
        if(mSession.getCurrentEvent() != null) {
            mCountdown = new CountDownTimer(mSession.getCurrentEvent().getEnd() - mSession.getDrift(), 1000) {
                public void onFinish() { }

                public void onTick(long millisUntilFinished) {
                    refreshTitle();
                }
            }.start();
        }
    }

    /** Update the song countdown timer in the application heading. */
    private void refreshTitle() {
        long end = mSession.getCurrentEvent().getEnd();
        long utc = System.currentTimeMillis() / 1000;
        long seconds = (end - utc) - mSession.getDrift();

        seconds = Math.max(0, seconds);
        long minutes = seconds / 60;
        seconds %= 60;

        Resources r = getResources();
        int id = mSession.getStationId();
        String stationName = (mSession.hasStations()) ? mSession.getStation(id).getName() : null;
        String title = (stationName != null) ? stationName : r.getString(R.string.app_name);
        String state = r.getString(R.string.label_nottunedin);

        if(!mSession.hasCredentials()) {
            state = r.getString(R.string.label_anonymous);
        }
        else if(mSession.isTunedIn()) {
            state = r.getString(R.string.label_tunedin);
        }

        setTitle(String.format("[%2d:%02d] %s (%s)", minutes, seconds, title, state));
    }

    /** Make the election drawer update to the latest known election. */
    private void updateElection() {
        SongListAdapter adapter = new SongListAdapter(
                this,
                R.layout.item_song,
                new ArrayList<Song>(Arrays.asList(mSession.getNextEvent().cloneSongs()))
        );

        // Set vote deadline for when the song ends.
        adapter.setDeadline(mSession.getCurrentEvent().getEnd() - mSession.getDrift());

        // Open the drawer if the user can vote.
        boolean canVote = !mSession.hasLastVote() && mSession.isTunedIn();
        setDrawerState(canVote);

        // Mark song as voted if we have a last vote.
        if(mSession.hasLastVote()) {
            boolean found = false;
            for(int i = 0; i < adapter.getCount(); i++) {
                Song s = adapter.getItem(i);
                if(s.getElectionEntryId() == mSession.getLastVoteId()) {
                    adapter.setStatusLabel(s.getId(), R.string.label_voted);
                    found = true;
                    break;
                }
            }
            if(!found) {
                Log.i(TAG, String.format("Found a last vote ID (%d), but it is not in the current election list!",
                                         mSession.getLastVoteId())
                );
            }
        }

        // Last so the UI reflects most up to date data.
        ((ListView)findViewById(R.id.np_electionList)).setAdapter(adapter);
    }

    /** Update the request list UI to the latest known request list. */
    private void updateRequests() {
        Song songs[];

        if(mSession.hasRequests()){
            songs = mSession.cloneRequests();
        }
        else {
            songs = new Song[0];
        }

        TouchInterceptor requestList = (TouchInterceptor) findViewById(R.id.np_request_list);
        SongListAdapter adapter = new SongListAdapter(
                this,
                R.layout.item_song_request,
                new ArrayList<Song>(Arrays.asList(songs))
        );
        adapter.setShowAlbum(true);
        adapter.setShowRating(false);
        adapter.setShowArtist(false);
        adapter.setShowRequest(false);
        requestList.setAdapter(adapter);

        resyncRequests();
    }

    /** Update the visibility of the "no pending requests" message. */
    private void resyncRequests() {
        TouchInterceptor requestList = (TouchInterceptor) findViewById(R.id.np_request_list);
        SongListAdapter adapter = (SongListAdapter) requestList.getAdapter();
        if(adapter != null) {
            int visibility = (adapter.getCount()) > 0 ? View.GONE : View.VISIBLE;
            findViewById(R.id.np_request_overlay).setVisibility(visibility);
        }
    }

    /**
     * Updates the song title, album title, and artists in the user interface.
     * 
     * @param current
     *            the current song that's playing.
     */
    private void updateSongInfo(Song current) {
        ((TextView) findViewById(R.id.np_songTitle)).setText(current.getTitle());
        ((TextView) findViewById(R.id.np_albumTitle)).setText(current.getDefaultAlbum().getName());
        ((TextView) findViewById(R.id.np_artist)).setText(current.collapseArtists());

        ImageView accent = (ImageView)findViewById(R.id.np_accent);
        TextView requestor = (TextView)findViewById(R.id.np_requestor);
        Resources r = getResources();

        if(current.isRequest()) {
            accent.setImageResource(R.drawable.accent_song_hilight);
            requestor.setVisibility(View.VISIBLE);
            requestor.setText(String.format(r.getString(R.string.label_requestor), current.getRequestor()));
        }
        else {
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
        final Album album = current.getDefaultAlbum();
        ((HorizontalRatingBar) findViewById(R.id.np_songRating))
           .setBothValues(current.getUserRating(), current.getCommunityRating());

        ((HorizontalRatingBar) findViewById(R.id.np_albumRating))
            .setBothValues(album.getUserRating(), album.getCommunityRating());
    }

    /**
     * Executes when a "rate song" request has finished.
     * 
     * @param result
     *            the result the server issued
     */
    private void onRateSong(SongRating rating) {
        ((HorizontalRatingBar) findViewById(R.id.np_songRating))
                .setPrimaryValue(rating.getUserRating());

        ((HorizontalRatingBar) findViewById(R.id.np_albumRating))
            .setPrimaryValue(rating.getDefaultAlbumRating().getUserRating());
    }

    /**
     * Sets the album art to the provided Bitmap, or a default image if art is
     * null.
     * 
     * @param art
     *            desired album art
     */
    private void updateAlbumArt(Bitmap art) {
        if(art == null) {
            art = BitmapFactory.decodeResource(getResources(), R.drawable.noart);
        }

        ((ImageView) findViewById(R.id.np_albumArt)).setImageBitmap(art);
    }

    /** Receives schedule updates from the sync service. */
    private BroadcastReceiver mEventUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, String.format("Got '%s'", action));
            Context ctx = NowPlayingActivity.this;
            setProgressBarIndeterminateVisibility(false);

            // Success -- update the UI and receive further updates.
            if(SyncService.BROADCAST_EVENT_UPDATE.equals(action)) {
                onScheduleSync();
                if(mSession.hasCredentials()) {
                    Intent local = new Intent(NowPlayingActivity.this, SyncService.class);
                    local.setAction(SyncService.ACTION_SYNC);
                    startService(local);
                }
            }
            // Credentials are bad. Automatic sync will not be attempted. The
            // user may re-initiate and an anonymous request is attempted.
            else if(SyncService.BROADCAST_REAUTHENTICATE.equals(action)) {
                mSession.clearUserInfo();
                Toast.makeText(ctx, R.string.msg_authenticationFailure, Toast.LENGTH_LONG).show();
            }
            // General failure -- connection or otherwise.
            else if(SyncService.BROADCAST_EVENT_UPDATE_FAILED.equals(action)) {
                Toast.makeText(ctx, R.string.msg_eventUpdateFailure, Toast.LENGTH_LONG).show();
            }
            else if(action == null) {
                Log.w(TAG, "Sync service sent no action -- ignoring.");
            }
            else {
                Log.w(TAG,
                        String.format("Sync service sent unrecognized action '%s'.", action)
                );
            }

        }
    };

    /** Removes a sequence of requests in background */
    private class RemoveTask extends RainwaveAsyncTask<Void, Void, Boolean> {
        private Song mSong;

        public RemoveTask(Song song) {
            mSong = song;
        }

        @Override
        protected Boolean getResult(Void... args) throws RainwaveException {
            mSession.deleteRequest(mSong);
            return true;
        }

        @Override
        protected void onFailure(RainwaveException raised) {
            Toast.makeText(NowPlayingActivity.this, raised.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private class RateTask extends RainwaveAsyncTask<Void, Void, SongRating> {
        private Song mSong;
        private float mRating;

        public RateTask(Song song, float rating) {
            mSong = song;
            mRating = rating;
        }

        @Override
        protected SongRating getResult(Void... params) throws RainwaveException {
            return mSession.rateSong(mSong.getId(), mRating);
        }

        @Override
        protected void onSuccess(SongRating result) {
            onRateSong(result);
        }

        @Override
        protected void onFailure(RainwaveException raised) {
            Toast.makeText(NowPlayingActivity.this, raised.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /** Reorders requests in background. */
    private class ReorderTask extends RainwaveAsyncTask<Song, Void, Song[]> {
        @Override
        protected Song[] getResult(Song... requests) throws RainwaveException {
            return mSession.reorderRequests(requests);
        }

        @Override
        protected void onFailure(RainwaveException raised) {
            Toast.makeText(NowPlayingActivity.this, raised.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /** AsyncTask for voting in the election. */
    private class VoteTask extends RainwaveAsyncTask<Void, Void, Boolean> {
        private Song mSong;

        VoteTask(Song song) {
            mSong = song;
        }

        @Override
        protected void onPreExecute() {
            ListView electionList = (ListView) findViewById(R.id.np_electionList);
            SongListAdapter adapter = (SongListAdapter) electionList.getAdapter();
            adapter.clearStatusLabels();
            adapter.setStatusLabel(mSong.getId(), R.string.label_voting);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected Boolean getResult(Void... params) throws RainwaveException {
            mSession.vote(mSong.getElectionEntryId());
            return true;
        }

        @Override
        protected void onSuccess(Boolean result) {
            ListView electionList = (ListView) findViewById(R.id.np_electionList);
            SongListAdapter adapter = (SongListAdapter) electionList.getAdapter();
            adapter.setStatusLabel(mSong.getId(), R.string.label_voted);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onFailure(RainwaveException raised) {
            Toast.makeText(NowPlayingActivity.this, raised.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /** Dialog identifiers */
    public static final int
        DIALOG_STATION_PICKER = 0xb1c7;
}
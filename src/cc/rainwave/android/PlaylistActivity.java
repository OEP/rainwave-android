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

import java.util.Comparator;
import java.util.Locale;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.Toast;
import cc.rainwave.android.adapters.AlbumListAdapter;
import cc.rainwave.android.adapters.FilterableAdapter;
import cc.rainwave.android.adapters.SongListAdapter;
import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.Album;
import cc.rainwave.android.api.types.Artist;
import cc.rainwave.android.api.types.RainwaveException;
import cc.rainwave.android.api.types.Song;

public class PlaylistActivity extends ListActivity {

    private static final String TAG = "PlaylistActivity";

    private Song mSongs[];

    private Session mSession;

    private int mMode = MODE_TOP_LEVEL;

    private Comparator<Song> mAlbumSongComparator = new Comparator<Song>() {
        @Override
        public int compare(Song lhs, Song rhs) {
            if(lhs.isCooling() ^ rhs.isCooling()) {
                return (lhs.isCooling()) ? 1 : -1;
            }

            return lhs.getTitle().toLowerCase(Locale.US).compareTo(rhs.getTitle().toLowerCase(Locale.US));
        }
    };

    private Comparator<Song> mArtistSongComparator = new Comparator<Song>() {
        @Override
        public int compare(Song lhs, Song rhs) {
            final String lhsAlbumName = lhs.getDefaultAlbum().getName();
            final String rhsAlbumName = rhs.getDefaultAlbum().getName();
            if(!lhsAlbumName.equals(rhsAlbumName)) {
                return lhsAlbumName.compareTo(rhsAlbumName);
            }

            return lhs.getTitle().toLowerCase(Locale.US).compareTo(rhs.getTitle().toLowerCase(Locale.US));
        }
    };

    private Comparator<Artist> mArtistComparator = new Comparator<Artist>() {
        @Override
        public int compare(Artist lhs, Artist rhs) {
            return lhs.getName().toLowerCase(Locale.US).compareTo(rhs.getName().toLowerCase(Locale.US));
        }
    };

    private Comparator<Album> mAlbumComparator = new Comparator<Album>() {
        @Override
        public int compare(Album lhs, Album rhs) {
            if(lhs.isCooling() ^ rhs.isCooling()) {
                return (lhs.isCooling()) ? 1 : -1;
            }

            return lhs.getName().toLowerCase(Locale.US).compareTo(rhs.getName().toLowerCase(Locale.US));
        }
    };


    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_playlist);
        setListeners();
    }

    public void onResume() {
        super.onResume();
        mSession = Session.getInstance(this);
        fetchDataIfNeeded();
    }

    public boolean onKeyDown(int keyCode, KeyEvent ev) {
        switch(keyCode) {
        case KeyEvent.KEYCODE_BACK:
            if(mMode == MODE_DETAIL_ALBUM || mMode == MODE_DETAIL_ARTIST) {
                mMode = MODE_TOP_LEVEL;
                refreshData();
                return true;
            }
        }
        return super.onKeyDown(keyCode, ev);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        switch(mMode) {
        case MODE_DETAIL_ALBUM:
        case MODE_DETAIL_ARTIST:
            break;
        default:
            return;
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist_context_menu, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Song s = (Song) getListView().getItemAtPosition(info.position);
        menu.setHeaderTitle(s.getTitle());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.request:
            Song s = (Song) getListView().getItemAtPosition(info.position);
            new RequestTask().execute(s.getId());
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_refresh:
            // clear result list and hide the filter
            setListAdapter(null);
            hideFilter();

            // check if we're looking at albums or artists and refresh
            if(isByAlbum()) {
                fetchAlbums(true);
            }
            else {
                fetchArtists(true);
            }
            return true;
        }

        return false;
    }

    private void setListeners() {
        RadioButton a = (RadioButton) findViewById(R.id.by_album);
        RadioButton b = (RadioButton) findViewById(R.id.by_artist);
        EditText filterText = (EditText) findViewById(R.id.filterText);

        OnClickListener tmp = new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMode = MODE_TOP_LEVEL;
                refreshData();
            }
        };

        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(mMode == MODE_TOP_LEVEL && isByAlbum()) {
                    ListAdapter adapter = getListAdapter();
                    hideFilter();
                    setListAdapter(null);
                    mMode = MODE_DETAIL_ALBUM;
                    Album choice = (Album) adapter.getItem(position);
                    new FetchDetailedAlbumTask().execute(choice.getId());
                }
                else if(mMode == MODE_TOP_LEVEL) {
                    ListAdapter adapter = getListAdapter();
                    hideFilter();
                    setListAdapter(null);
                    mMode = MODE_DETAIL_ARTIST;
                    Artist choice = (Artist) adapter.getItem(position);
                    new FetchDetailedArtistTask().execute(choice.getId());
                }
            }
        });

        a.setOnClickListener(tmp);
        b.setOnClickListener(tmp);

        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable e) {
                // do nothing
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                if(mMode != MODE_TOP_LEVEL) return;
                if(isByAlbum()) {
                    ArrayAdapter<Album> adapter = (ArrayAdapter<Album>) getListAdapter();
                    if(adapter == null) return;
                    adapter.getFilter().filter(s);
                }
                else {
                    ArrayAdapter<Artist> adapter = (ArrayAdapter<Artist>) getListAdapter();
                    if(adapter == null) return;
                    adapter.getFilter().filter(s);
                }
            }

        });

        registerForContextMenu(getListView());
    }

    private EditText hideFilter() {
        EditText filterText = (EditText) findViewById(R.id.filterText);
        filterText.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(filterText.getWindowToken(), 0);
        return filterText;
    }

    /** Resynchronizes the UI with the data in the current session. */
    private void refreshData() {
        EditText filterText = hideFilter();
        if(isByAlbum() && mMode == MODE_TOP_LEVEL) {
            if(mSession.getAlbums() != null) {
                AlbumListAdapter adapter = new AlbumListAdapter(this, android.R.layout.simple_list_item_1, mSession.getAlbums());
                adapter.sort(mAlbumComparator);
                filterText.setText("");
                filterText.setHint(R.string.msg_filterAlbum);
                setListAdapter(adapter);
                filterText.setVisibility(View.VISIBLE);
            }
            else {
                setListAdapter(null);
                fetchAlbums(false);
            }
        }
        else if(mMode == MODE_TOP_LEVEL){
            if(mSession.getArtists() != null) {
                ArrayAdapter<Artist> adapter = new FilterableAdapter<Artist>(this, android.R.layout.simple_list_item_1, mSession.getArtists());
                adapter.sort(mArtistComparator);
                filterText.setHint(R.string.msg_filterArtist);
                filterText.setText("");
                setListAdapter(adapter);
                filterText.setVisibility(View.VISIBLE);
            }
            else {
                setListAdapter(null);
                fetchArtists(false);
            }
        }
        else if(mMode == MODE_DETAIL_ALBUM || mMode == MODE_DETAIL_ARTIST) {
            if(mSongs != null) {
                SongListAdapter adapter = new SongListAdapter(this, R.layout.item_song, mSongs);
                adapter.sort((mMode == MODE_DETAIL_ALBUM) ? mAlbumSongComparator : mArtistSongComparator);
                adapter.setShowAlbum(mMode != MODE_DETAIL_ALBUM);
                adapter.setShowArtist(mMode != MODE_DETAIL_ARTIST);
                adapter.setShowRequest(false);
                adapter.setShowRating(true);
                adapter.setShowCooldown(true);
                setListAdapter(adapter);
            }
        }
    }

    private void fetchDataIfNeeded() {
        if(isByAlbum() && mSession.getAlbums() == null) {
            fetchAlbums(false);
        }
        else if(mSession.getArtists() == null) {
            fetchArtists(false);
        }
        refreshData();
    }

    /**
     * Fetch an entire list of albums if needed.
     * 
     * @param forceRefresh
     *            always perform the fetch
     */
    private void fetchAlbums(final boolean forceRefresh) {
        if(forceRefresh || mSession.getAlbums() == null) {
            new FetchAlbumsTask().execute();
            return;
        }
        refreshData();
    }

    /**
     * Fetch an entire list of artists if needed.
     * 
     * @param forceRefresh
     *            always perform the fetch
     */
    private void fetchArtists(final boolean forceRefresh) {
        if(forceRefresh || mSession.getArtists() == null) {
            new FetchArtistsTask().execute();
            return;
        }
        refreshData();
    }

    private boolean isByAlbum() {
        RadioButton b = (RadioButton) findViewById(R.id.by_album);
        return b.isChecked();
    }

    private class FetchAlbumsTask extends AsyncTask<String,String,Album[]> {

        @Override
        protected Album[] doInBackground(String... args) {
            try {
                return mSession.fetchAlbums();
            } catch (RainwaveException e) {
                Log.w(TAG, "API error", e);
            }
            return null;
        }

        protected void onPostExecute(Album result[]) {
            if(result == null) {
                Toast.makeText(PlaylistActivity.this, R.string.msg_genericError, Toast.LENGTH_SHORT).show();
                return;
            }
            refreshData();
        }
    }

    private class FetchArtistsTask extends AsyncTask<String,String,Artist[]> {

        @Override
        protected Artist[] doInBackground(String... args) {
            try {
                return mSession.fetchArtists();
            } catch (RainwaveException e) {
                Log.w(TAG, "API error", e);
            }
            return null;
        }

        protected void onPostExecute(Artist result[]) {
            if(result == null) {
                Toast.makeText(PlaylistActivity.this, R.string.msg_genericError, Toast.LENGTH_SHORT).show();
                return;
            }
            refreshData();
        }
    }

    private class FetchDetailedArtistTask extends AsyncTask<Integer,String,Artist> {
        @Override
        protected Artist doInBackground(Integer ... args) {
            int artist_id = args[0];
            try {
                return mSession.fetchDetailedArtist(artist_id);
            } catch (RainwaveException e) {
                Log.e(TAG, "API error", e);
            }
            return null;
        }

        protected void onPostExecute(Artist result) {
            if(result == null) {
                Toast.makeText(PlaylistActivity.this, R.string.msg_genericError, Toast.LENGTH_SHORT).show();
                return;
            }
            mSongs = result.cloneSongs(mSession.getStationId());
            refreshData();
        }
    }

    private class FetchDetailedAlbumTask extends AsyncTask<Integer,String,Album> {
        @Override
        protected Album doInBackground(Integer ... args) {
            int album_id = args[0];
            try {
                return mSession.fetchDetailedAlbum(album_id);
            } catch (RainwaveException e) {
                Log.w(TAG, "API error", e);
            }
            Log.d(TAG, "Error fetching album!");
            return null;
        }

        protected void onPostExecute(Album result) {
            if(result == null){
                Toast.makeText(PlaylistActivity.this, R.string.msg_genericError, Toast.LENGTH_SHORT).show();
                return;
            }
            mSongs = result.cloneSongs();
            refreshData();
        }
    }

    private class RequestTask extends AsyncTask<Integer, Integer, Song[]> {
        @Override
        protected Song[] doInBackground(Integer... args) {
            int song_id = args[0];

            try {
                return mSession.submitRequest(song_id);
            } catch (RainwaveException e) {
                Log.e(TAG, "API Error: ", e);
            }
            return null;
        }

        protected void onPostExecute(Song[] songs) {
            if(songs == null){
                Toast.makeText(PlaylistActivity.this, R.string.msg_genericError, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(PlaylistActivity.this, R.string.msg_requested, Toast.LENGTH_SHORT).show();
        }
    }

    public static final int
        MODE_TOP_LEVEL = 1,
        MODE_DETAIL_ALBUM = 2,
        MODE_DETAIL_ARTIST = 4;
}

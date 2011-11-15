package cc.rainwave.android;

import java.io.IOException;
import java.util.Comparator;

import cc.rainwave.android.adapters.SongListAdapter;
import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.Album;
import cc.rainwave.android.api.types.Artist;
import cc.rainwave.android.api.types.RainwaveException;
import cc.rainwave.android.api.types.Song;
import cc.rainwave.android.views.CountdownView;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class PlaylistActivity extends ListActivity {
	
	private static final String TAG = "PlaylistActivity";
	
	private Artist mArtists[];
	
	private Album mAlbums[];
	
	private Song mSongs[];
	
	private Session mSession;
	
	private FetchAlbumsTask mFetchAlbums;
	
	private FetchArtistsTask mFetchArtists;
	
	private FetchDetailedAlbumTask mFetchAlbum;
	
	private FetchDetailedArtistTask mFetchArtist;
	
	private int mMode = MODE_TOP_LEVEL;
	
	private Comparator<Song> mAlbumSongComparator = new Comparator<Song>() {
		@Override
		public int compare(Song lhs, Song rhs) {
			if(lhs.isCooling() && !rhs.isCooling()) {
				return 1;
			}
			else if(rhs.isCooling() && !lhs.isCooling()) {
				return -1;
			}
			
			return lhs.song_title.compareTo(rhs.song_title);
		}
	};
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_playlist);
		setListeners();
	}
	
	public void onResume() {
		super.onResume();
		initializeSession();
		fetchDataIfNeeded();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent ev) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if(mMode == MODE_DETAIL_ALBUM || mMode == MODE_DETAIL_ARTIST) {
				mMode = MODE_TOP_LEVEL;
				setTheData();
				return true;
			}
		}
		return super.onKeyDown(keyCode, ev);
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
        } catch (IOException e) {
            Rainwave.showError(this, e);
        }
    }
    
    private void setListeners() {
    	RadioButton a = (RadioButton) findViewById(R.id.by_album);
    	RadioButton b = (RadioButton) findViewById(R.id.by_artist);
    	
    	OnClickListener tmp = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMode = MODE_TOP_LEVEL;
				setTheData();
			}
    	};
    	
    	getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if(mMode == MODE_TOP_LEVEL && isByAlbum()) {
					ArrayAdapter<Album> adapter = (ArrayAdapter<Album>) getListAdapter();
					setListAdapter(null);
					mMode = MODE_DETAIL_ALBUM;
					Album choice = adapter.getItem(position);
					fetchAlbum(choice.album_id);
				}
				else if(mMode == MODE_TOP_LEVEL) {
					ArrayAdapter<Artist> adapter = (ArrayAdapter<Artist>) getListAdapter();
					setListAdapter(null);
					mMode = MODE_DETAIL_ARTIST;
					Artist choice = adapter.getItem(position);
					fetchArtist(choice.artist_id);
				}
			}
    	});
    	
    	a.setOnClickListener(tmp);
    	b.setOnClickListener(tmp);
    }
    
    private void setTheData() {
    	if(isByAlbum() && mMode == MODE_TOP_LEVEL) {
    		if(mAlbums != null) {
    			ArrayAdapter<Album> adapter = new ArrayAdapter<Album>(this, android.R.layout.simple_list_item_1, mAlbums);
    			adapter.sort(new Comparator<Album>() {
					@Override
					public int compare(Album a, Album b) {
						return a.compareTo(b);
					}
    			});
    			setListAdapter(adapter);
    		}
    		else {
    			setListAdapter(null);
    			fetchAlbums();
    		}
    	}
    	else if(mMode == MODE_TOP_LEVEL){
    		if(mArtists != null) {
    			ArrayAdapter<Artist> adapter = new ArrayAdapter<Artist>(this, android.R.layout.simple_list_item_1, mArtists);
    			adapter.sort(new Comparator<Artist>() {
					@Override
					public int compare(Artist a, Artist b) {
						return a.compareTo(b);
					}
    			});
    			setListAdapter(adapter);
    		}
    		else {
    			setListAdapter(null);
    			fetchArtists();
    		}
    	}
    	else if(mMode == MODE_DETAIL_ALBUM || mMode == MODE_DETAIL_ARTIST) {
    		Log.d("DERP", "The mode is: " + mMode);
    		if(mSongs != null) {
    			ArrayAdapter<Song> adapter = new ArrayAdapter<Song>(this, R.layout.item_song_playlist, mSongs) {
    				public View getView(int position, View convertView, ViewGroup parent) {
    					if(convertView == null) {
    						Song s = getItem(position);
    						Context ctx = getContext();
    						LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    						
    						convertView = inflater.inflate(R.layout.item_song_playlist, null);
    						
    						TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
    						TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);

    						CountdownView circle = (CountdownView) convertView.findViewById(R.id.circle);
    						TextView time = (TextView) convertView.findViewById(R.id.time);
    						TextView cooldown = (TextView) convertView.findViewById(R.id.cooldown);
    						
    						// We should have at least this much for both views.
    						text1.setText(s.song_title);
    						time.setText(s.getLengthString());
    						
    						if(s.isCooling()) {
    							// TODO: Set cooldown.
    						}
    						else {
    							cooldown.setVisibility(View.GONE);
    						}
    						
    						if(mMode == MODE_DETAIL_ALBUM) {
    							circle.setVisibility(View.VISIBLE);
	    						circle.setBoth(s.song_rating_user,s.song_rating_avg);
	    						text2.setText(s.collapseArtists());
    						}
    						else {
    							circle.setVisibility(View.GONE);
    							Log.d("HERP", "Setting the album name to " + s.album_name);
    							text2.setText(s.album_name);
    						}
    					}
    					
    					return convertView;
    				}
    			};
    			
    			// TODO: Properly choose comparator.
    			adapter.sort(mAlbumSongComparator);
    			setListAdapter(adapter);
    		}
    	}
    }

	private void fetchDataIfNeeded() {
		if(isByAlbum() && mAlbums == null) {
			fetchAlbums();
		}
		else if(mArtists == null) {
			fetchArtists();
		}
	}
	
	private void stopAlbumFetch() {
		if(mFetchAlbum != null) {
			mFetchAlbum.cancel(true);
			mFetchAlbum = null;
		}
	}
	
	private void stopArtistFetch() {
		if(mFetchArtist != null) {
			mFetchArtist.cancel(true);
			mFetchArtist = null;
		}
	}
	
	private void fetchAlbums() {
		if(mFetchAlbums != null || mAlbums != null) return;
		mFetchAlbums = new FetchAlbumsTask();
		mFetchAlbums.execute();
	}
	
	private void fetchArtists() {
		if(mFetchArtists != null || mArtists != null) return;
		mFetchArtists = new FetchArtistsTask();
		mFetchArtists.execute();
	}
	
	private void fetchAlbum(int album_id) {
		stopArtistFetch();
		if(mFetchAlbum != null) return;
		mFetchAlbum = new FetchDetailedAlbumTask();
		mFetchAlbum.execute(album_id);
	}
	
	private void fetchArtist(int artist_id) {
		stopAlbumFetch();
		if(mFetchArtist != null) return;
		mFetchArtist = new FetchDetailedArtistTask();
		mFetchArtist.execute(artist_id);
	}
	
	private boolean isByAlbum() {
		RadioButton b = (RadioButton) findViewById(R.id.by_album);
		return b.isChecked();
	}
	
	private class FetchAlbumsTask extends AsyncTask<String,String,Album[]> {
		@Override
		protected Album[] doInBackground(String... args) {
			try {
				return mSession.getAlbums();
			} catch (IOException e) {
				Rainwave.showError(PlaylistActivity.this, e);
				Log.e(TAG, "IO Error: " + e);
			} catch (RainwaveException e) {
				Rainwave.showError(PlaylistActivity.this, e);
				Log.e(TAG, "API Error: " + e);
			}
			return null;
		}
		
		protected void onPostExecute(Album result[]) {
			if(result == null) return;
			mAlbums = result;
			updateView();
		}
	}
	
	private class FetchArtistsTask extends AsyncTask<String,String,Artist[]> {
		@Override
		protected Artist[] doInBackground(String... args) {
			try {
				return mSession.getArtists();
			} catch (IOException e) {
				Rainwave.showError(PlaylistActivity.this, e);
				Log.e(TAG, "IO Error: " + e);
			} catch (RainwaveException e) {
				Rainwave.showError(PlaylistActivity.this, e);
				Log.e(TAG, "API Error: " + e);
			}
			return null;
		}
		
		protected void onPostExecute(Artist result[]) {
			if(result == null) return;
			mArtists = result;
			updateView();
		}
	}
	
	private class FetchDetailedArtistTask extends AsyncTask<Integer,String,Artist> {
		@Override
		protected Artist doInBackground(Integer ... args) {
			int artist_id = args[0];
			try {
				return mSession.getDetailedArtist(artist_id);
			} catch (IOException e) {
				Rainwave.showError(PlaylistActivity.this, e);
				Log.e(TAG, "IO Error: " + e);
			} catch (RainwaveException e) {
				Rainwave.showError(PlaylistActivity.this, e);
				Log.e(TAG, "API Error: " + e);
			}
			return null;
		}
		
		protected void onPostExecute(Artist result) {
			if(result == null) {
				mFetchArtist = null;
				return;
			}
			mSongs = result.songs;
			updateView();
			mFetchArtist = null;
		}
	}
	
	private class FetchDetailedAlbumTask extends AsyncTask<Integer,String,Album> {
		@Override
		protected Album doInBackground(Integer ... args) {
			int album_id = args[0];
			try {
				return mSession.getDetailedAlbum(album_id);
			} catch (IOException e) {
				Rainwave.showError(PlaylistActivity.this, e);
				Log.e(TAG, "IO Error: " + e);
			} catch (RainwaveException e) {
				Rainwave.showError(PlaylistActivity.this, e);
				Log.e(TAG, "API Error: " + e);
			}
			return null;
		}
		
		protected void onPostExecute(Album result) {
			if(result == null){
				mFetchAlbum = null;
				return;
			}
			mSongs = result.song_data;
			updateView();
			mFetchAlbum = null;
		}
	}
	
	private void updateView() {
		mHandler.obtainMessage(SET_THE_DATA).sendToTarget();
	}
	
    private Handler mHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		Bundle data = msg.getData();
    		switch(msg.what) {
    		case SET_THE_DATA:
    			setTheData();
    			break;
    		}
    	}

    	
    };
	
    public static final int
    	MODE_TOP_LEVEL = 1,
    	MODE_DETAIL_ALBUM = 2,
    	MODE_DETAIL_ARTIST = 4;
    
	public static final int
		SET_THE_DATA = 0x5E7DA7A;
}

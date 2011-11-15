package cc.rainwave.android;

import java.io.IOException;
import java.util.Comparator;

import cc.rainwave.android.adapters.SongListAdapter;
import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.Album;
import cc.rainwave.android.api.types.Artist;
import cc.rainwave.android.api.types.RainwaveException;
import android.app.Activity;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class PlaylistActivity extends ListActivity {
	
	private static final String TAG = "PlaylistActivity";
	
	private Artist mArtists[];
	
	private Album mAlbums[];
	
	private Session mSession;
	
	private FetchAlbumsTask mFetchAlbums;
	
	private FetchArtistsTask mFetchArtists;
	
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
				setTheData();
			}
    	};
    	
    	a.setOnClickListener(tmp);
    	b.setOnClickListener(tmp);
    }
    
    private void setTheData() {
    	if(isByAlbum()) {
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
    			fetchAlbums();
    		}
    	}
    	else {
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
    			fetchArtists();
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
		SET_THE_DATA = 0x5E7DA7A;
}

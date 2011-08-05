package cc.rainwave.android;

import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.Song;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ElectionListAdapter extends BaseAdapter {
	private static final String TAG = "ElectionListAdapter";
	
	private View mViews[];
	
	private Song mSongs[];
	
	private Context mContext;
	
	private Session mSession;
	
	private CountdownTask mCountdownTask;
	
	public ElectionListAdapter(Context ctx, Session session, Song songs[]) {
		mContext = ctx;
		mSession = session;
		mSongs = songs;
		mViews = new View[mSongs.length];
	}
	
	public void startCountdown(int i) {
		if(mCountdownTask == null) {
			mCountdownTask = new CountdownTask(i);
			mCountdownTask.execute();
		}
	}

	@Override
	public int getCount() {
		return (mSongs == null) ? 0 : mSongs.length;
	}

	@Override
	public Object getItem(int i) {
		return (mSongs == null) ? null : mSongs[i];
	}

	@Override
	public long getItemId(int i) {
		return (mSongs == null) ? -1 : mSongs[i].song_id;
	}
	
	@Override
	public View getView(int i, View convertView, ViewGroup parent) {
		if(convertView == null) {
			Song s = mSongs[i];
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.item_song, null);
			
			((TextView)convertView.findViewById(R.id.election_songTitle)).setText(s.song_title);
			((TextView)convertView.findViewById(R.id.election_songAlbum)).setText(s.album_name);
			((TextView)convertView.findViewById(R.id.election_songArtist)).setText(s.collapseArtists());
			
			reflectSong(((CountdownView)convertView.findViewById(R.id.election_songRating)), s);
		}
		
		mViews[i] = convertView;
		return convertView;
	}
	
	private void reflectSong(CountdownView v, Song s) {
		v.setBoth(s.song_rating_user, s.song_rating_avg);
	}
	
	public void submitVote(int selection) {
		Log.d(TAG, "Submit vote here!");
	}
	
	private class CountdownTask extends AsyncTask<Integer, Integer, Boolean> {
		private int mSelection;
		
		private CountdownView mCountdownView;
		
		private Song mSong;
		
		public CountdownTask(int selection) {
			mSelection = selection;
			View v = ElectionListAdapter.this.mViews[mSelection];
			mCountdownView = (CountdownView) v.findViewById(R.id.election_songRating);
			mSong = mSongs[selection];
		}
		
		@Override
		protected Boolean doInBackground(Integer ...params) {
			mCountdownView.setMax(5.0f);
			mCountdownView.setBoth(5.0f, 0.0f);
			mCountdownView.setShowValue(true);
			mCountdownView.setAlternateText(R.string.label_voting);
			while(mCountdownView.getPrimary() > 0) {
				mCountdownView.decrementPrimary(0.1f);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return false;
				}
			}
			
			return true;
		}
		
		protected void onPostExecute(Boolean result) {
			if(result == true) {
				submitVote(mSelection);
			}
			else {
				reflectSong(mCountdownView, mSong);
			}
		}
	}

}

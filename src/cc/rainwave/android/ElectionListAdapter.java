package cc.rainwave.android;

import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.Song;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ElectionListAdapter extends BaseAdapter {
	
	private Song mSongs[];
	
	private Context mContext;
	
	private Session mSession;
	
	public ElectionListAdapter(Context ctx, Session session) {
		mContext = ctx;
		mSession = session;
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
	
	public void setSongs(Song songs[]) {
		mSongs = songs;
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
		}
		return convertView;
	}

}

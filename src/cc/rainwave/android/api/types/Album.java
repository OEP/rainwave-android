package cc.rainwave.android.api.types;

public class Album implements Comparable<Album> {
	public boolean album_favourite;
	
	public long album_lowest_oa;
	
	public float album_rating_avg, album_rating_user;
	
	public String album_name;
	
	public int album_id;
	
	public Song song_data[];
	
	public String toString() {
		return album_name;
	}

	@Override
	public int compareTo(Album a) {
		return album_name.compareTo(a.album_name);
	}
}

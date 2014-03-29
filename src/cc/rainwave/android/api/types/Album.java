package cc.rainwave.android.api.types;

import android.os.Parcel;
import android.os.Parcelable;

public class Album implements Parcelable, Comparable<Album> {
	public boolean album_favourite;
	
	public long album_lowest_oa;
	
	public float album_rating_avg, album_rating_user;
	
	public String name;
	public String art;
	
	public int album_id;
	
	public Song song_data[];
	
	public String toString() {
		return name;
	}
	
	public boolean isCooling() {
		return getCooldown() > 0;
	}
	
	public long getCooldown() {
		long utc = System.currentTimeMillis() / 1000;
		return album_lowest_oa - utc;
	}

	@Override
	public int compareTo(Album a) {
		return name.compareTo(a.name);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(album_lowest_oa);
		dest.writeFloat(album_rating_avg);
		dest.writeFloat(album_rating_user);
		dest.writeString(name);
		dest.writeString(art);
		dest.writeInt(album_id);
		dest.writeParcelableArray(song_data, flags);
	}
}

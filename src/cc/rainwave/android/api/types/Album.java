package cc.rainwave.android.api.types;

import android.os.Parcel;
import android.os.Parcelable;

public class Album implements Parcelable, Comparable<Album> {
	public boolean album_favourite;
	
	public long album_lowest_oa;
	
	public float rating, rating_user;
	
	public String name;
	public String art;
	
	public int id;
	
	public Song song_data[];
	
	private Album(Parcel source) {
		this.album_lowest_oa = source.readLong();
		this.rating = source.readFloat();
		this.rating_user = source.readFloat();
		this.name = source.readString();
		this.art = source.readString();
		this.id = source.readInt();
		final Parcelable tmpSongs[] = source.readParcelableArray(Song[].class.getClassLoader());
		
		this.song_data = new Song[tmpSongs.length];
		for(int i = 0; i < tmpSongs.length; i++) {
			this.song_data[i] = (Song) tmpSongs[i];
		}
	}

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
		dest.writeFloat(rating);
		dest.writeFloat(rating_user);
		dest.writeString(name);
		dest.writeString(art);
		dest.writeInt(id);
		dest.writeParcelableArray(song_data, flags);
	}
	
    public static final Parcelable.Creator<Album> CREATOR
    = new Parcelable.Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}

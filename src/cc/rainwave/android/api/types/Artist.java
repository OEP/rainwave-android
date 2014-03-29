package cc.rainwave.android.api.types;

import android.os.Parcel;
import android.os.Parcelable;

public class Artist implements Parcelable, Comparable<Artist> {
	public int id;
	public String name;
	
	public Song[] songs;
	
	private Artist(Parcel in) {
	    id = in.readInt();
	    name = in.readString();
	}
	
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }
    
    public static final Parcelable.Creator<Artist> CREATOR
        = new Parcelable.Creator<Artist>() {
            @Override
            public Artist createFromParcel(Parcel source) {
                return new Artist(source);
            }

            @Override
            public Artist[] newArray(int size) {
                return new Artist[size];
            }
        };

	@Override
	public int compareTo(Artist another) {
		return name.compareTo(another.name);
	}
	
	public String toString() {
		return name;
	}
}

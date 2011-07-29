package cc.rainwave.android.api.types;

import android.os.Parcel;
import android.os.Parcelable;

public class Artist implements Parcelable {
	public int artist_id;
	public String artist_name;
	
	private Artist(Parcel in) {
	    artist_id = in.readInt();
	    artist_name = in.readString();
	}
	
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(artist_id);
        dest.writeString(artist_name);
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
}

package cc.rainwave.android.api.types;

import android.os.Parcel;
import android.os.Parcelable;

public class Station implements Parcelable {
	public String oggstream;
	public String description;
	public String stream;
	public String name;
	public int id;
	
	private Station(Parcel in) {
		oggstream = in.readString();
		description = in.readString();
		stream = in.readString();
		name = in.readString();
		id = in.readInt();
	}
	
	public String toString() {
		return name;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(oggstream);
		dest.writeString(description);
		dest.writeString(stream);
		dest.writeString(name);
		dest.writeInt(id);
	}
	
    public static final Parcelable.Creator<Station> CREATOR
    = new Parcelable.Creator<Station>() {
        @Override
        public Station createFromParcel(Parcel source) {
            return new Station(source);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };
}

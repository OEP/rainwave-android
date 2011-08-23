package cc.rainwave.android.api.types;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {
	public Song song_data[];
	public long sched_endtime;

    private Event(Parcel source) {
        Parcelable tmp[] = source.readParcelableArray(Song[].class.getClassLoader());
        sched_endtime = source.readLong();
        song_data = new Song[tmp.length];
        
        for(int i = 0; i < tmp.length; i++) {
            song_data[i] = (Song) tmp[i];
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelableArray(song_data, flags);
        dest.writeLong(sched_endtime);
    }
    
    public static final Parcelable.Creator<Event> CREATOR
    = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}

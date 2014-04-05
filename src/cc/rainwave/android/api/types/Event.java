package cc.rainwave.android.api.types;

import java.lang.reflect.Type;

import cc.rainwave.android.api.JsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {
	private int mId;
	private Song mSongs[];
	private long mEnd;
	
	/** Can't instantiate directly. */
	private Event() {}

    private Event(Parcel source) {
        Parcelable tmp[] = source.readParcelableArray(Song[].class.getClassLoader());
        mEnd = source.readLong();
        mSongs = new Song[tmp.length];
        
        for(int i = 0; i < tmp.length; i++) {
            mSongs[i] = (Song) tmp[i];
        }
    }
    
    public int getId() {
    	return mId;
    }
    
    public int getSongCount() {
    	return mSongs.length;
    }
    
    public Song[] cloneSongs() {
    	return mSongs.clone();
    }
    
    /**
     * Get the currently playing song (for a "current event" only).
     * @return current playing song
     */
    public Song getCurrentSong() {
    	return getSong(0);
    }
    
    public Song getSong(int i) {
    	return mSongs[i];
    }
    
    public long getEnd() {
    	return mEnd;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelableArray(mSongs, flags);
        dest.writeLong(mEnd);
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
    
	public static class Deserializer implements JsonDeserializer<Event> {
		@Override
		public Event deserialize(
			JsonElement element, Type type,	JsonDeserializationContext ctx
		) throws JsonParseException {
			final Event a = new Event();
			a.mId = JsonHelper.getInt(element, "id");
			a.mEnd = JsonHelper.getLong(element, "end");
			a.mSongs = ctx.deserialize(JsonHelper.getJsonArray(element, "songs"), Song[].class);
			return a;
		}
	}
}

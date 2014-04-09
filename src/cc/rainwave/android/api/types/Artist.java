package cc.rainwave.android.api.types;

import java.lang.reflect.Type;

import cc.rainwave.android.api.JsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import android.os.Parcel;
import android.os.Parcelable;

public class Artist implements Parcelable, Comparable<Artist> {
    /** Artist ID */
    private int mId;
    
    /** Artist name */
    private String mName;
    
    /** Songs attributed to artist. */
    private Song[] mSongs;
    
    /** Can't instantiate directly. */
    private Artist() {}
    
    /** Utility constructor for placeholder objects. */
    public Artist(int id, String name) {
        mId = id;
        mName = name;
    }
    
    private Artist(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
    }
    
    public int getId() {
        return mId;
    }
    
    public String getName() {
        return mName;
    }
    
    public Song[] cloneSongs() {
        return mSongs.clone();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
    }
    
    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel source) {
            return new Artist(source);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
        
    public static class Deserializer implements JsonDeserializer<Artist> {
        @Override
        public Artist deserialize(
            JsonElement element, Type type,    JsonDeserializationContext ctx
        ) throws JsonParseException {
            final Artist a = new Artist();
            a.mId = JsonHelper.getInt(element, "id");
            a.mName = JsonHelper.getString(element, "name");
            a.mSongs = ctx.deserialize(JsonHelper.getJsonArray(element, "songs", null), Song[].class);
            return a;
        }
    }
    
    @Override
    public int compareTo(Artist another) {
        return mName.compareTo(another.mName);
    }
    
    public String toString() {
        return mName;
    }
}

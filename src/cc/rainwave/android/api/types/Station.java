package cc.rainwave.android.api.types;

import java.lang.reflect.Type;

import cc.rainwave.android.api.JsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import android.os.Parcel;
import android.os.Parcelable;

public class Station implements Parcelable {
    /** Description of radio station. */
    private String mDescription;

    /** URL to main stream. */
    private String mStream;

    /** Name of radio station. */
    private String mName;

    /** ID of radio station. */
    private int mId;

    /** Can't instantiate directly. */
    private Station() {}

    private Station(Parcel in) {
        mDescription = in.readString();
        mStream = in.readString();
        mName = in.readString();
        mId = in.readInt();
    }

    /**
     * Get the radio station's name.
     * @return the name
     */
    public String getName() {
        return mName;
    }

    /**
     * Get the radio station's description.
     * @return the description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Get the URL to the main stream.
     * @return url to stream
     */
    public String getMainStream() {
        return mStream;
    }

    /**
     * Get the station's ID.
     * @return station id
     */
    public int getId() {
        return mId;
    }

    public String toString() {
        return mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDescription);
        dest.writeString(mStream);
        dest.writeString(mName);
        dest.writeInt(mId);
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

    public static class Deserializer implements JsonDeserializer<Station> {
        @Override
        public Station deserialize(
            JsonElement element, Type type,    JsonDeserializationContext ctx
        ) throws JsonParseException {
            final Station s = new Station();
            s.mDescription = JsonHelper.getString(element, "description");
            s.mStream = JsonHelper.getString(element, "stream");
            s.mName = JsonHelper.getString(element, "name");
            s.mId = JsonHelper.getInt(element, "id");
            return s;
        }
    }
}

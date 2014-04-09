package cc.rainwave.android.api.types;

import java.lang.reflect.Type;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;
import cc.rainwave.android.api.JsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class Song implements Parcelable, Comparable<Song> {

    /** UTC timestamp when cooldown period is over. */
    private long mCooldownEnd;

    /** Flag indicating song is on its cooldown period. */
    private boolean mCooldown;

    /** Song ID */
    private int mId;

    /** Election entry ID */
    private int mEntryId;

    /** Request status enumeration code. */
    private int mIsRequest;

    /** Song length in seconds */
    private int mSecondsLong;

    /** Song title */
    private String mTitle;

    /** Song artists */
    private Artist mArtists[];

    /** Song albums */
    private Album mAlbums[];

    /** Username which requested song */
    private String mRequestor;

    /** User's rating of song */
    private float mUserRating;

    /** Community's rating of song */
    private float mRating;

    /** Can't instantiate directly. */
    private Song() {}

    private Song(Parcel in) {
        mId = in.readInt();
        mEntryId = in.readInt();
        mIsRequest = in.readInt();
        mTitle = in.readString();
        Parcelable tmpArtists[] = in.readParcelableArray(Artist[].class.getClassLoader());
        Parcelable tmpAlbums[] = in.readParcelableArray(Album[].class.getClassLoader());
        mRequestor = in.readString();

        mArtists = new Artist[tmpArtists.length];
        for(int i = 0; i < tmpArtists.length; i++) {
            mArtists[i] = (Artist) tmpArtists[i];
        }

        mAlbums = new Album[tmpAlbums.length];
        for(int i = 0; i < tmpAlbums.length; i++) {
            mAlbums[i] = (Album) tmpAlbums[i];
        }
    }

    public float getUserRating() {
        return mUserRating;
    }

    /**
     * Set the user's rating for this song. Note that this does not result
     * in an API call. 
     * 
     */
    public void setUserRating(float userRating) {
        mUserRating = Math.max(MIN_RATING, Math.min(MAX_RATING, userRating));
    }

    public float getCommunityRating() {
        return mRating;
    }

    public int getId() {
        return mId;
    }

    public int getElectionEntryId() {
        return mEntryId;
    }

    public int getAlbumCount() {
        return mAlbums.length;
    }

    public Album getAlbum(int index) {
        return mAlbums[index];
    }

    public Album getDefaultAlbum() {
        return getAlbum(0);
    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * Get who requested the song.
     * @return username of who requested song, or null if not in an election or not requested
     */
    public String getRequestor() {
        return mRequestor;
    }

    public boolean isRequest() {
        return getRequestor() != null;
    }

    public String collapseArtists() {
        return collapseArtists(", ", " & ");
    }

    public String collapseArtists(String comma, String and) {
        if(mArtists == null) return "???";
        switch(mArtists.length) {
            case 0: return "???";
            case 1: return mArtists[0].getName();
            case 2: return mArtists[0].getName() + " " + and + " " + mArtists[1].getName();
            default:
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < mArtists.length; i++) {
                    sb.append(mArtists[i].getName());

                    if(i < mArtists.length - 2) {
                        sb.append(comma);
                    }
                    else if(i == mArtists.length - 2) {
                        sb.append(and);
                    }
                }
                return sb.toString();
        }
    }

    public String toString() {
        return mTitle;
    }

    public String getLengthString() {
        int m = mSecondsLong / 60;
        int s = mSecondsLong % 60;
        return String.format(Locale.US, "%d:%02d", m, s);
    }

    /**
     * Check if song is on it's cooldown period.
     * 
     * @return true if cooldown period is active, false otherwise
     */
    public boolean isCooling() {
        return mCooldown;
    }

    /**
     * Get the time when cooldown is over.
     * 
     * @return number of seconds remaining in cooldown period
     */
    public long getCooldown() {
        long utc = System.currentTimeMillis() / 1000;
        return mCooldownEnd - utc;
    }

    @Override
    public int compareTo(Song s) {
        final String album_name = mAlbums[0].getName();
        final String other_album_name = s.mAlbums[0].getName();
        if(album_name != null && other_album_name != null && !album_name.equals(other_album_name)) {
            return album_name.compareTo(other_album_name);
        }
        return mTitle.compareTo(s.mTitle);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeInt(mEntryId);
        dest.writeInt(mIsRequest);
        dest.writeString(mTitle);
        dest.writeParcelableArray(mArtists, flags);
        dest.writeParcelableArray(mAlbums, flags);
        dest.writeString(mRequestor);
    }

    public static final Parcelable.Creator<Song> CREATOR
    = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public static class Deserializer implements JsonDeserializer<Song> {
        @Override
        public Song deserialize(
            JsonElement element, Type type,    JsonDeserializationContext ctx
        ) throws JsonParseException {
            final Song s = new Song();
            s.mEntryId = JsonHelper.getInt(element, "entry_id", -1);
            s.mRating = JsonHelper.getFloat(element, "rating", 0.0f);
            s.mUserRating = JsonHelper.getFloat(element, "rating_user", 0.0f);
            s.mTitle = JsonHelper.getString(element, "title");
            s.mId = JsonHelper.getInt(element, "id");
            s.mSecondsLong = JsonHelper.getInt(element, "length");
            s.mRequestor = JsonHelper.getString(element, "elec_request_username", null);
            s.mCooldown = JsonHelper.getBoolean(element, "cool", false);
            s.mCooldownEnd = JsonHelper.getLong(element, "cool_end", 0);
            s.mAlbums = ctx.deserialize(JsonHelper.getJsonArray(element, "albums", null), Album[].class);
            s.mArtists = ctx.deserialize(JsonHelper.getJsonArray(element, "artists", null), Artist[].class);

            // some end points may return artist_parseable but not artist
            if(s.mArtists == null) {
                String artistParseable = JsonHelper.getString(element, "artist_parseable", null);
                if(artistParseable != null) {
                    String parts[] = artistParseable.split(",");
                    s.mArtists = new Artist[parts.length];
                    for(int i = 0; i < parts.length; i++) {
                        final String part = parts[i];

                        String idname[] = part.split("[|]");
                        if(idname.length != 2) {
                            throw new JsonParseException(
                                String.format("%s split on pipe has %d part(s).", artistParseable, idname.length)
                            );
                        }

                        final int id = Integer.valueOf(idname[0]);
                        final String name = idname[1];

                        s.mArtists[i] = new Artist(id, name);
                    }
                }
            }
            return s;
        }
    }

    /** LiquidRain's definition for the field 'elec_isrequest' */
    public static final int
        ELEC_FULFILLED_REQUEST = 4,
        ELEC_RANDOM_REQUEST = 3,
        ELEC_NORMAL = 2,
        ELEC_CONFLICT = 0;

    /** Minimum rating for a song. */
    public static final float MIN_RATING = 1.0f;

    /** Maximum rating for a song. */
    public static final float MAX_RATING = 5.0f;

}

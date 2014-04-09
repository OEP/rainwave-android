package cc.rainwave.android.api.types;

import java.lang.reflect.Type;

import cc.rainwave.android.api.JsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class SongRating {
    private float mUserRating;
    private AlbumRating[] mUpdatedAlbums;
    
    /** Can't instantiate directly. */
    private SongRating() {}
    
    public float getUserRating() {
        return mUserRating;
    }
    
    public AlbumRating getDefaultAlbumRating() {
        return getAlbumRating(0);
    }
    
    private AlbumRating getAlbumRating(int i) {
        return mUpdatedAlbums[i];
    }
    
    public static class AlbumRating {
        private float mUserRating;
        
        public float getUserRating() {
            return mUserRating;
        }
        
        public static class Deserializer implements JsonDeserializer<AlbumRating> {
            @Override
            public AlbumRating deserialize(
                final JsonElement element, Type type, JsonDeserializationContext ctx
            ) throws JsonParseException {
                final AlbumRating a = new AlbumRating();
                a.mUserRating = JsonHelper.getFloat(element, "rating_user");
                return a;
            }
        }
    }
    
    public static class Deserializer implements JsonDeserializer<SongRating> {
        @Override
        public SongRating deserialize(
            final JsonElement element, Type type, JsonDeserializationContext ctx
        ) throws JsonParseException {
            final SongRating a = new SongRating();
            a.mUserRating = JsonHelper.getFloat(element, "rating_user");
            a.mUpdatedAlbums = ctx.deserialize(JsonHelper.getJsonArray(element, "updated_album_ratings"), AlbumRating[].class);
            return a;
        }
    }
}

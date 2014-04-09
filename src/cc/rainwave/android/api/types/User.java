package cc.rainwave.android.api.types;

import java.lang.reflect.Type;

import cc.rainwave.android.api.JsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class User {
    /** User's username */
    private String mUsername;
    
    /** Numeric user ID */
    private int mUserId;
    
    /** True if user is tuned in. */
    private boolean mTunedIn = false;
    
    /** Can't instantiate directly. */
    private User() {}
    
    public boolean getTunedIn() {
        return mTunedIn;
    }
    
    public int getId() {
        return mUserId;
    }
    
    public String getUsername() {
        return mUsername;
    }
    
    public static class Deserializer implements JsonDeserializer<User> {
        @Override
        public User deserialize(
            JsonElement element, Type type,    JsonDeserializationContext ctx
        ) throws JsonParseException {
            final User u = new User();
            u.mUsername = JsonHelper.getString(element, "name");
            u.mUserId = JsonHelper.getInt(element, "id");
            u.mTunedIn = JsonHelper.getBoolean(element, "tuned_in");
            return u;
        }
    }
}

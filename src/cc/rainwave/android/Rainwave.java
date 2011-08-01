package cc.rainwave.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Rainwave {
    
    public static String getUrl(Context ctx, String url) {
        return getPreferences(ctx).getString(PREFS_URL, url);
    }
    
    public static String getUserId(Context ctx) {
        return getPreferences(ctx).getString(PREFS_USERID, null);
    }
    
    public static String getKey(Context ctx) {
        return getPreferences(ctx).getString(PREFS_KEY, null);
    }
    
    public static SharedPreferences getPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
    
    public static final String
        PREFS_URL = "pref_url",
        PREFS_USERID = "pref_userId",
        PREFS_KEY = "pref_key";
}

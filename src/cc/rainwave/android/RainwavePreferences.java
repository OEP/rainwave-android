package cc.rainwave.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Preference singleton.
 * @author pkilgo
 *
 */
@SuppressLint("CommitPrefEdits") // Commits happen in check()
public class RainwavePreferences {
    private static final String TAG = RainwavePreferences.class.getSimpleName();

    /** The singleton. */
    private static RainwavePreferences sInstance;

    /** Shared preferences object. */
    private SharedPreferences mPreferences;

    private RainwavePreferences(Context ctx) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        if(getVersion() < CURRENT_VERSION) {
            migrate(getVersion());
        }
        setVersion(CURRENT_VERSION);
    }

    /**
     * Get the singleton.
     * @param ctx used only if the singleton is constructed
     * @return preference singleton
     */
    public static RainwavePreferences getInstance(Context ctx) {
        if(sInstance == null) {
            sInstance = new RainwavePreferences(ctx);
        }
        return sInstance;
    }

    /**
     * Returns the current version of the preferences.
     * @return current version of preferences, or 0 if none is stored
     */
    public int getVersion() {
        return mPreferences.getInt(VERSION, 0);
    }

    /**
     * Get flag indicating if elections should show automatically.
     * @return true if elections should show automatically, true by default
     */
    public boolean getAutoshowElection() {
        return mPreferences.getBoolean(AUTOSHOW_ELECTION, true);
    }

    /**
     * Get the last URL for a Session.
     * @return last URL, or null if none is known
     */
    public String getUrl() { 
        return mPreferences.getString(URL, null);
    }

    /**
     * Get the stored user id.
     * @return the stored user id, or null
     */
    public String getUserId() {
        return mPreferences.getString(USERID, null);
    }

    /**
     * Get the last station ID stored.
     * @return the last station ID, or -1 if none is stored
     */
    public int getLastStationId() {
        return mPreferences.getInt(LASTSTATION, -1);
    }

    /**
     * Get last station ID stored.
     * @param defaultValue what to return if no last station is known
     * @return the last station ID, or the default value if not known
     */
    public int getLastStationId(int defaultValue) {
        return mPreferences.getInt(LASTSTATION, defaultValue);
    }

    /**
     * Get flag indicating if we should skip the landing.
     * @return true if we should skip landing, false by default
     */
    public boolean getSkipLanding() {
        return mPreferences.getBoolean(SKIPLANDING, false);
    }

    /**
     * Get the stored key.
     * @return the stored key, or null
     */
    public String getKey() {
        return mPreferences.getString(KEY, null);
    }

    /**
     * Stores user info in preferences.
     * @param userId the user ID to store
     * @param key the key to store
     * @return true if the commit operation succeeded
     */
    public boolean setUserInfo(String userId, String key) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(USERID, userId);
        editor.putString(KEY, key);
        return check(editor, "setUserInfo()");
    }

    /**
     * Stores the last station ID in preferences.
     * @param stationId the value to store
     * @return true if the commit operation succeeded
     */
    public boolean setLastStationId(int stationId) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(LASTSTATION, stationId);
        return check(editor, "setLastStationId()");
    }

    /**
     * Stores preference for skipping landing.
     * @param value true to skip landing page next time
     * @return true if the commit succeeded
     */
    public boolean setSkipLanding(boolean value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(SKIPLANDING, value);
        return check(editor, "setSkipLanding()");
    }

    /**
     * Clears all user preferences.
     * @return true if the operation succeeded
     */
    public boolean clear() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(KEY);
        editor.remove(SKIPLANDING);
        editor.remove(USERID);
        editor.remove(LASTSTATION);
        editor.remove(AUTOSHOW_ELECTION);
        return check(editor, "clear()");
    }

    /** Sets the current preferences version. */
    private void setVersion(int version) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(VERSION, version);
        check(editor, "setVersion()");
    }

    /** Migrate an old version of the preferences to a new one. */
    private void migrate(int oldVersion) {
        // placeholder implementation
        switch(oldVersion) {
        default: break;
        }
    }

    /** Utility method to log a bad result of a commit operation. */
    private boolean check(SharedPreferences.Editor editor, String tag) {
        boolean result = editor.commit();
        if(!result) {
            StringBuffer sb = new StringBuffer();
            if(tag != null) {
                sb.append(tag + ": ");
            }
            sb.append("Commit to preferences failed.");
            Log.w(TAG, sb.toString());
        }
        return result;
    }

    /** Key for URL preference. */
    private static final String URL = "pref_url";

    /** Key for skip landing preference. */
    private static final String SKIPLANDING = "pref_skipLanding";

    /** Key for last known user ID. */
    private static final String USERID = "pref_userId";

    /** Key for last station ID. */
    private static final String LASTSTATION = "pref_lastStation";

    /** Key for autoshowing election preference. */
    private static final String AUTOSHOW_ELECTION = "pref_autoshow_elections";

    /** Key for API key. */
    private static final String KEY = "pref_key";

    /** Key for last known preference version. */
    private static final String VERSION = "version";

    /** Current version of preferences. Used in migrations. */
    public static final int CURRENT_VERSION = 1;
}

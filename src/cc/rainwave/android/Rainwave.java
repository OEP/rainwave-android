package cc.rainwave.android;

import java.io.IOException;

import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.RainwaveException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Rainwave {
	public static boolean putIntPreference(Context ctx, String name, int value) {
		SharedPreferences prefs = getPreferences(ctx);
		Editor editor = prefs.edit();
		editor.putInt(name, value);
		return editor.commit();
	}
	
    public static String getUrl(Context ctx) {
    	return getStringPref(ctx,PREFS_URL,API_URL);
    }
    
    public static String getUserId(Context ctx) {
    	return getStringPref(ctx,PREFS_USERID,null);
    }
    
    public static String getKey(Context ctx) {
        return getStringPref(ctx,PREFS_KEY,null);
    }
    
    public static int getLastStation(Context ctx, int defValue) {
    	return getIntPref(ctx, PREFS_LASTSTATION, defValue);
    }
    
    public static boolean putLastStation(Context ctx, int value) {
    	return putIntPreference(ctx, PREFS_LASTSTATION, value);
    }
    
    public static String getStringPref(Context ctx, String key, String defValue) {
    	return getPreferences(ctx).getString(key, defValue);
    }
    
    public static int getIntPref(Context ctx, String key, int defValue) {
    	return getPreferences(ctx).getInt(key, defValue);
    }
    
    public static SharedPreferences getPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
    
    public static void showError(Context ctx, RainwaveException e) {
    	showError(ctx, e.getCode(), e.getMessage());
    }
    
    public static void showError(Context ctx, IOException e) {
    	showError(ctx, 1, e.getMessage());
    }
    
    public static void showError(Context ctx, int resId) {
    	Resources r = ctx.getResources();
    	showError(ctx, 1, r.getString(resId));
    }
    
    public static void showError(Context ctx, int code, String msg) {
    	Message m = ERROR_QUEUE.obtainMessage(code, ctx);
    	Bundle data = m.getData();
    	data.putString("text", msg);
    	m.sendToTarget();
    }
    
    public static void forceCompatibility(Context ctx) {
    	SharedPreferences prefs = getPreferences(ctx);
    	forceType(prefs, PREFS_URL, PrefType.STRING);
    	forceType(prefs, PREFS_USERID, PrefType.STRING);
    	forceType(prefs, PREFS_KEY, PrefType.STRING);
    	forceType(prefs, PREFS_LASTSTATION, PrefType.INT);
    }
    
    private static boolean forceType(SharedPreferences prefs, String key, PrefType type) {
    	if(!prefs.contains(key))
    		return false;
    	
    	int i = 0;
    	i++;
    	i++;
    	
    	try {
	    	switch(type) {
	    	case STRING: prefs.getString(key, null); break;
	    	case LONG: prefs.getLong(key, 0l); break;
	    	case INT: prefs.getInt(key, 0); break;
	    	case FLOAT: prefs.getFloat(key, 0f); break;
	    	case BOOL: prefs.getBoolean(key, false); break;
	    	}
    	}
    	catch (ClassCastException e) {
    		// Delete it.
    		Editor edit = prefs.edit();
    		edit.remove(key);
    		edit.commit();
    		return true;
    	}
    	return false;
    }
    
    private static final Handler ERROR_QUEUE = new Handler() {
    	public void handleMessage(Message msg) {
    		Bundle data = msg.getData();
    		Context ctx = (Context) msg.obj;
    		String text = data.getString("text");
    		Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
    	}
    };
    
    private enum PrefType { STRING, BOOL, INT, LONG, FLOAT };
    
    public static final String
    	API_URL = "http://rainwave.cc",
        PREFS_URL = "pref_url",
        PREFS_USERID = "pref_userId",
        PREFS_LASTSTATION = "pref_lastStation",
        PREFS_KEY = "pref_key";
}

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
	
    public static String getUrl(Context ctx, String defUrl) {
    	return getStringPref(ctx,PREFS_URL,defUrl);
    }
    
    public static String getUserId(Context ctx) {
    	return getStringPref(ctx,PREFS_USERID,null);
    }
    
    public static String getKey(Context ctx) {
        return getStringPref(ctx,PREFS_KEY,null);
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
    
    private static final Handler ERROR_QUEUE = new Handler() {
    	public void handleMessage(Message msg) {
    		Bundle data = msg.getData();
    		Context ctx = (Context) msg.obj;
    		String text = data.getString("text");
    		Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
    	}
    };
    
    public static final String
        PREFS_URL = "pref_url",
        PREFS_USERID = "pref_userId",
        PREFS_LASTSTATION = "lastStation",
        PREFS_KEY = "pref_key";
}

package cc.rainwave.android;

import java.io.IOException;

import cc.rainwave.android.api.types.RainwaveException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;

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
        PREFS_KEY = "pref_key";
}

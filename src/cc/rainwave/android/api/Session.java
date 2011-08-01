package cc.rainwave.android.api;

import cc.rainwave.android.api.types.ScheduleOrganizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Session {
	private static final String TAG = "Session";
	
	public String mStation = "1";
	
	public String mUserId;
	
	public String mKey;
	
	private URL mBaseUrl;
	
	/** Can't instantiate directly */
	private Session() { }
	
	public ScheduleOrganizer asyncGet()
	throws IOException {
		HttpURLConnection conn = getConnection(true, "get");
		
		Gson gson = getGson();
		JsonParser parser = new JsonParser();
		JsonElement json = parser.parse(getReader(conn));
		
		return gson.fromJson(json, ScheduleOrganizer.class);
	}
	
	public Bitmap fetchAlbumArt(String path) throws IOException {
	    URL url = new URL( getUrl(path) );
	    InputStream is = url.openStream();
	    return BitmapFactory.decodeStream(is);
	}
	
	public void setUserInfo(String userId, String key) {
		mUserId = userId;
		mKey = key;
	}
	
	private HttpURLConnection getConnection(boolean async, String request, String ... params)
	throws IOException {
		String path = String.format("%s/%s/%s", (async) ? "async" : "sync", mStation, request);
		
		if(mUserId != null && mKey != null) {
			
			// Extend the var-args into an array with 4 more slots.
			String tmp[] = (params != null) ? new String[params.length + 4] : new String[4];
			int begin = (params != null) ? params.length : 0;
			for(int i = 0; i < params.length; i++) {
				tmp[i] = params[i];
			}
			
			// Insert in the new data.
			tmp[begin] = NAME_USERID;
			tmp[begin+1] = mUserId;
			tmp[begin+2] = NAME_KEY;
			tmp[begin+3] = mKey;
			
			// Get new urlencoded string
			String paramString = HttpHelper.encodeParams(tmp);
			
			// Return HttpURLConnection
			return HttpHelper.makePost(mBaseUrl, path, paramString);
		}
		else {
			return HttpHelper.makeGet(mBaseUrl, path, "");
		}
		
		//TODO: Set time out for long polls.
	}
	
	private Reader getReader(HttpURLConnection conn)
	throws IOException {
		return new BufferedReader(new InputStreamReader(conn.getInputStream()));
	}
	
	private String getUrl(String path) {
	    if(path == null || path.length() == 0) return mBaseUrl.toString();
	    
	    if(path.charAt(0) != '/') {
	        return String.format("%s/%s", mBaseUrl.toString(), path);
	    }
	    else {
	        return String.format("%s%s", mBaseUrl.toString(), path);
	    }
	}
	
	private Gson getGson() {
	    GsonBuilder builder = new GsonBuilder();
	    builder.registerTypeAdapter(ScheduleOrganizer.class, new ScheduleOrganizer.Deserializer());
	    return builder.create();
	}
	
	public static Session makeSession() throws MalformedURLException {
		return makeSession(API_URL);
	}
	
	public static Session makeSession(String url) throws MalformedURLException {
		Session s = new Session();
		s.mBaseUrl = new URL(url);
		return s;
	}
	
	public static final String
		NAME_USERID = "user_id",
		NAME_KEY = "key",
//		API_URL = "http://students.mint.ua.edu/~pmkilgo/tmp";
		API_URL = "http://rainwave.cc";
}


package cc.rainwave.android.api;

import cc.rainwave.android.R;
import cc.rainwave.android.Rainwave;
import cc.rainwave.android.api.types.Album;
import cc.rainwave.android.api.types.Artist;
import cc.rainwave.android.api.types.Event;
import cc.rainwave.android.api.types.GenericResult;
import cc.rainwave.android.api.types.RainwaveException;
import cc.rainwave.android.api.types.Song;
import cc.rainwave.android.api.types.SongRating;
import cc.rainwave.android.api.types.Station;
import cc.rainwave.android.api.types.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

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
    
    private Context mContext;

    private int mStation = 1;

    private String mUserId;

    private String mKey;

    private URL mBaseUrl;
    
    private Event mCurrrentSchedule;
    
    /** Can't instantiate directly */
    private Session() { }

    public void info() throws IOException, RainwaveException {
    	final String path = "info";
    	try {
    		final JsonElement element = get(path);
    		// TODO: update schedules
    	}
    	catch(final JsonParseException exc) {
    		throw wrapException(exc, path);
    	}
    }
    
    public void sync() throws IOException, RainwaveException {
    	final String path = "sync";
    	try {
    		final JsonElement element = post(path);
    		// TODO: update schedules
    	}
    	catch(final JsonParseException exc) {
    		throw wrapException(exc, path);
    	}
    }

    public SongRating rateSong(int songId, float rating)
    throws IOException, RainwaveException {
    	final String path = "rate";
    	final String returns = "rate_result";
    	
    	rating = Math.max(1.0f, Math.min(rating, 5.0f));
    	
    	return requestObject(Method.POST, path, returns, true, SongRating.class,
			"song_id", String.valueOf(songId),
			"rating", String.valueOf(rating)
    	);
    }
    
    public void vote(int elecId)
	throws IOException, RainwaveException {
    	final String path = "vote";
    	final String returns = "vote_result";
    	
    	try {
	    	final JsonElement element = post(path,
    			"entry_id", String.valueOf(elecId)
	    	);
	    	checkError(JsonHelper.getChild(element, returns));
    	}
    	catch(final JsonParseException exc) {
    		throw wrapException(exc, path);
    	}
    }
    
    public Station[] getStations() throws IOException, RainwaveException {
    	final String path = "stations";
    	final String returns = "stations";
    	return requestObject(Method.POST, path, returns, false, Station[].class);
    }
    
    public Album[] getAlbums() throws IOException, RainwaveException {
    	final String path = "all_albums";
    	final String returns = "all_albums";
    	return requestObject(Method.POST, path, returns, false, Album[].class);
    }
    
    public Artist[] getArtists() throws IOException, RainwaveException {
    	final String path = "all_artists";
    	final String returns = "all_artists";
    	return requestObject(Method.POST, path, returns, false, Artist[].class);
    }
    
    public Artist getDetailedArtist(int artist_id) throws IOException, RainwaveException {
    	final String path = "artist";
    	final String returns = "artist";
    	
    	return requestObject(Method.POST, path, returns, false, Artist.class,
    		"id", String.valueOf(artist_id)
    	);
    }
    
    public Album getDetailedAlbum(int album_id) throws IOException, RainwaveException {
    	final String path = "album";
    	final String returns = "album";
    	
    	return requestObject(Method.POST, path, returns, false, Album.class,
    		"id", String.valueOf(album_id)
    	);
    }
    
    public Song[] submitRequest(int song_id) throws IOException, RainwaveException {
    	final String path = "request";
    	
    	final JsonElement root = post(path, "song_id", String.valueOf(song_id));
    	try {
    		final Gson gson = getGson();
    		checkError(JsonHelper.getChild(root, "request_result"));
    		return gson.fromJson(JsonHelper.getChild(root, "requests"), Song[].class);
    	}
    	catch(final JsonParseException exc) {
    		throw wrapException(exc, path);
    	}
    }
    
    /**
     * Fetch a full resolution album art.
     * 
     * @param path base url to album art
     * @return bitmap of album art
     * @throws IOException
     */
    public Bitmap fetchAlbumArt(String path) throws IOException {
    	return fetchAlbumArtHelper(path + ".jpg");
    }

    /**
     * Fetch a minimum width album art. The returned bitmap is guaranteed to
     * be at least the requested width.
     * 
     * @param path base url to album art
     * @param width minimum width required
     * @return bitmap of album art
     * @throws IOException
     */
    public Bitmap fetchAlbumArt(String path, int width) throws IOException {
    	if(width <= 120) {
    		return fetchAlbumArtHelper(path + "_120.jpg");
    	}
    	else if(width <= 240) {
    		return fetchAlbumArtHelper(path + "_240.jpg");
    	}
    	else {
    		return fetchAlbumArt(path);
    	}
    }
    
    private Bitmap fetchAlbumArtHelper(String path) throws IOException {
        URL url = new URL(getUrl(path));
    	Log.d(TAG, "GET " + url.toString());
        InputStream is = url.openStream();
        return BitmapFactory.decodeStream(is);
    }
    
    public Song[] reorderRequests(Song requests[])
    		throws IOException, RainwaveException {
    	final String path = "request";
    	
    	final JsonElement root = post(path,
			"order_requests", 
			"order", Rainwave.makeRequestQueueString(requests)
		);
    	try {
    		final Gson gson = getGson();
    		checkError(JsonHelper.getChild(root, "order_requests_result"));
    		return gson.fromJson(JsonHelper.getChild(root, "requests"), Song[].class);
    	}
    	catch(final JsonParseException exc) {
    		throw wrapException(exc, path);
    	}
    }
    
    public void deleteRequest(Song request)
    		throws IOException, RainwaveException {
    	post(
    			"delete_request", 
    			"song_id", String.valueOf(request.getId())
    	);
    }

    public void setUserInfo(String userId, String key) {
        mUserId = userId;
        mKey = key;
    }
    
    public void setStation(int stationId) {
    	mStation = stationId;
    	Rainwave.putLastStation(mContext, stationId);
    }
    
    public String getUrl() {
    	return mBaseUrl.toString();
    }
    
    public int getStationId() {
    	return mStation;
    }
    
    public boolean isAuthenticated() {
        return mUserId != null && mKey != null && mUserId.length() > 0 && mKey.length() > 0;
    }
    
    private JsonElement get(String path, String... params)
    throws IOException, RainwaveException {
    	return request(Method.GET, path, params);
    }
    
    private JsonElement post(String path, String... params)
    throws IOException, RainwaveException {
    	return request(Method.POST, path, params);
    }
    
    private JsonElement request(final Method method, String path, String... params)
            throws IOException, RainwaveException {
        // Construct arguments
        Arguments httpArgs = new Arguments(params);
        httpArgs.put(NAME_STATION, String.valueOf(mStation));

        if(this.isAuthenticated()) {
          httpArgs.put(NAME_USERID, mUserId);
          httpArgs.put(NAME_KEY, mKey);
        }

        HttpURLConnection conn;
        switch(method) {
        case POST:
        	conn = HttpHelper.makePost(mBaseUrl, path, httpArgs.encode());
        	break;
        case GET:
        	conn = HttpHelper.makeGet(mBaseUrl, String.format("%s?%s", path, httpArgs.encode()), null);
        	break;
        default:
        	throw new IllegalArgumentException("Unhandled HTTP method!");
        }
        
        JsonParser parser = new JsonParser();
        return parser.parse(getReader(conn));
    }
    
    /**
     * Fetches a single object from the API.
     * 
     * @param method either Method.GET or Method.POST
     * @param path endpoint name
     * @param name member name 
     * @param checkError check for the success in a member named "success"
     * @param classOfT
     * @param params
     * @return
     * @throws RainwaveException
     * @throws IOException
     */
    private <T> T requestObject(
    	final Method method, final String path, final String name,
    	final boolean checkError, Class<T> classOfT, final String... params
    ) throws RainwaveException, IOException {
        // Convert the json into Java objects.
        Gson gson = getGson();
        final JsonElement json = request(method, path, params);
        
        try {
        	final JsonElement element = JsonHelper.getChild(json, name);
        	if(checkError) {
        		checkError(element);
        	}
        	return gson.fromJson(element, classOfT);
        }
        catch(JsonParseException e) {
        	throw wrapException(e, path);
        }
    }
    
    /**
     * Check for errors in a JSON response. Checks the value of a member
     * name called "success" and if it is false, throw an error containing
     * the contents of a field called "text".
     * 
     * @param element JSON object to check
     * @throws RainwaveException if an error is detected
     */
    private void checkError(final JsonElement element) throws RainwaveException {
    	if(!JsonHelper.getBoolean(element, "success")) {
    		throw new RainwaveException(0,
    			JsonHelper.getString(element, "text",
    				mContext.getString(R.string.msg_genericError)
    			)
    		);
    	}
    }
    
    private Reader getReader(HttpURLConnection conn)
            throws IOException {
        return new BufferedReader(new InputStreamReader(conn.getInputStream()));
    }
    
    private RainwaveException wrapException(final JsonParseException exc, final String path)
    throws RainwaveException {
    	Resources r = mContext.getResources();
    	String msg = String.format(r.getString(R.string.msgfmt_parseError), path, exc.getMessage());
    	throw new RainwaveException(0, msg);
    	
    }

    private String getUrl(String path) throws MalformedURLException {
        if (path == null || path.length() == 0)
            return mBaseUrl.toString();
        final URL url = new URL(mBaseUrl, path);
        return url.toString();
    }

    private Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Album.class, new Album.Deserializer());
        builder.registerTypeAdapter(Song.class, new Song.Deserializer());
        builder.registerTypeAdapter(Event.class, new Event.Deserializer());
        builder.registerTypeAdapter(Artist.class, new Artist.Deserializer());
        builder.registerTypeAdapter(User.class, new User.Deserializer());
        builder.registerTypeAdapter(Station.class, new Station.Deserializer());
        builder.registerTypeAdapter(SongRating.class, new SongRating.Deserializer());
        builder.registerTypeAdapter(SongRating.AlbumRating.class, new SongRating.AlbumRating.Deserializer());
        return builder.create();
    }

    public static Session makeSession(Context ctx) throws MalformedURLException {
        Session s = new Session();
        String url = Rainwave.getUrl(ctx);
        s.mContext = ctx;
        s.mBaseUrl = new URL(url);
        s.mStation = Rainwave.getLastStation(ctx, s.mStation);
        s.setUserInfo(Rainwave.getUserId(ctx), Rainwave.getKey(ctx));
        return s;
    }
    
    public static Session makeSession(Context ctx, String user, String key) throws MalformedURLException {
        Session s = new Session();
        String url = Rainwave.getUrl(ctx);
        s.mContext = ctx;
        s.mBaseUrl = new URL(url);
        s.mStation = Rainwave.getLastStation(ctx, s.mStation);
        s.setUserInfo(user,key);
        return s;
    }
    
    private static enum Method {
    	GET, POST
    }

    public static final String
            NAME_STATION = "sid",
            NAME_USERID = "user_id",
            NAME_KEY = "key";
}

package cc.rainwave.android.api;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpHelper {
    private static final String TAG = "HttpHelper";
    
	/**
	 * Create an <code>HttpURLConnection</code> object and make it post
	 * its parameters to the server.
	 * @param baseUrl The base URL of the server.
	 * @param path The path to make the POST to
	 * @param params URL-encoded parameters
	 * @param cookie to send to the server
	 * @return an <code>HttpURLConnection</code> for the connection
	 * @throws IOException
	 */
	public static HttpURLConnection makePost(URL baseUrl, String path, String params)
	throws IOException {
		URL url = new URL(String.format("%s/%s", baseUrl.toString(), path));
		Log.d(TAG, "POST " + url.toString() + " " + params);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		connection.setRequestProperty("Content-Length", Integer.toString(params.getBytes().length));
		connection.setRequestProperty("Content-Language", "en-US");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		
		// Send the request
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(params);
		wr.flush();
		wr.close();
		return connection;
	}
	
	/**
	 * Construct and return an <code>HttpURLConnection</code> object
	 * to use for a GET connection.
	 * @param baseUrl The base URL of the server
	 * @param path The path to make the GET call to.
	 * @param cookie The cookie to send to the server (null if no cookie)
	 * @return an <code>HttpURLConnection</code> object representing the established connection
	 * @throws IOException if there was trouble establishing the connection
	 */
	public static HttpURLConnection makeGet(URL baseUrl, String path)
	throws IOException {
		URL url = new URL(String.format("%s/%s", baseUrl.toString(), path));
		Log.d(TAG, "GET " + url.toString());
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Language", "en-US");
		
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(false);
		
		return connection;
	}
}

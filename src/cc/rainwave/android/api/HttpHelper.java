package cc.rainwave.android.api;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class HttpHelper {
    private static final String TAG = "HttpHelper";
    
	/**
	 * Create an <code>HttpURLConnection</code> object and make it post
	 * its parameters to the server.
	 * @param baseUrl The base URL of the server.
	 * @param path The path to make the POST to
	 * @param params URL-encoded parameters
	 * @return an <code>HttpURLConnection</code> for the connection
	 * @throws IOException
	 */
	public static HttpURLConnection makePost(URL baseUrl, String path, String params)
	throws IOException {
		return makePost(baseUrl, path, params, null);
	}
	
	
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
	public static HttpURLConnection makePost(URL baseUrl, String path, String params, String cookie)
	throws IOException {
		URL url = new URL(String.format("%s/%s", baseUrl.toString(), path));
		Log.d(TAG, "POST " + url.toString() + " " + params);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		// Set cookie if it was given
		if(cookie != null) {
			connection.setRequestProperty("Cookie", cookie);
		}
		
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
	public static HttpURLConnection makeGet(URL baseUrl, String path, String cookie)
	throws IOException {
		URL url = new URL(String.format("%s/%s", baseUrl.toString(), path));
		Log.d(TAG, "GET " + url.toString());
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Language", "en-US");
		
		// Set cookie if it was given
		if(cookie != null) {
			connection.setRequestProperty("Cookie", cookie);
		}
		
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(false);
		
		return connection;
	}
	
	/**
	 * Take an arbitrary list of arguments and return a URL-encoded
	 * String representation of the arguments.
	 * 		Note: This takes even-numbered lists of arguments
	 * @param args An alternating list of key/value payloads to URL-encode
	 * @return String representation of the key/value pairs, URL-encoded
	 * @throws UnsupportedEncodingException if "UTF-8" is not permissible
	 */
	public static String encodeParams(String ... args)
	throws UnsupportedEncodingException {
		if(args.length % 2 != 0) {
			throw new IllegalArgumentException("Must have a multiple of two arguments");
		}
		
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < args.length; i+=2) {
			buffer.append(URLEncoder.encode(args[i], "UTF-8"));
			buffer.append('=');
			buffer.append(URLEncoder.encode(args[i+1], "UTF-8"));
			buffer.append('&');
		}
		// Get rid of trailing '&'
		buffer.deleteCharAt(buffer.length() - 1);
		return buffer.toString();
	}
}

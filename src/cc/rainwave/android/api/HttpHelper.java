package cc.rainwave.android.api;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import android.util.Log;

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
		URL url = new URL(String.format("%s/%s", baseUrl.toString(), path));
		Log.d(TAG, "POST " + url.toString() + " " + params);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		connection.setRequestProperty("Content-Length", Integer.toString(params.getBytes().length));
		
		// FIXME: Investigate proper usage of Content-Language
		connection.setRequestProperty("Content-Language", "en-US");
		setAcceptLanguage(connection);
		
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
	 * @return an <code>HttpURLConnection</code> object representing the established connection
	 * @throws IOException if there was trouble establishing the connection
	 */
	public static HttpURLConnection makeGet(URL baseUrl, String path)
	throws IOException {
		URL url = new URL(String.format("%s/%s", baseUrl.toString(), path));
		Log.d(TAG, "GET " + url.toString());
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		
		// FIXME: Investigate proper usage of Content-Language
		connection.setRequestProperty("Content-Language", "en-US");
		setAcceptLanguage(connection);
		
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(false);
		
		return connection;
	}
	
	
	/** Sets the Accept-Language header according to default locale. */
	private static void setAcceptLanguage(HttpURLConnection conn) {
		Locale locale = Locale.getDefault();
		
		// Do nothing if no language is set.
		if(locale.getLanguage().length() == 0) {
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		
		// Add country information if available.
		if(locale.getCountry().length() > 0) {
			sb.append(locale.getLanguage() + "-" + locale.getCountry() + ",");
		}
		
		// Add just language (Q-score taken from Firefox)
		sb.append(locale.getLanguage() + ";q=0.5");
		
		conn.setRequestProperty("Accept-Language", sb.toString());
	}
}

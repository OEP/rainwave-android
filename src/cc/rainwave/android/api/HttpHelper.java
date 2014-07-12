/*
 * Copyright (c) 2013, Paul M. Kilgo
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of Paul Kilgo nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

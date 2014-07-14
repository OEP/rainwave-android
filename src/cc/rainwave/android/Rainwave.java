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

package cc.rainwave.android;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import cc.rainwave.android.api.types.Song;

public class Rainwave {
    /**
     * Makes a comma-delimited string out of an array of songs
     * delineating the value of Song.requestq_id.
     * @param requests
     * @return CSV string
     */
    public static String makeRequestQueueString(Song requests[]) {
        if(requests == null || requests.length == 0) return "";
        if(requests.length == 1) return String.valueOf(requests[0].getId());

        StringBuilder sb = new StringBuilder();
        sb.append(requests[0].getId());

        for(int i = 1; i < requests.length; i++) {
            sb.append(",");
            sb.append(requests[i].getId());
        }

        return sb.toString();
    }

    /**
     * Parse a Rainwave Uri.
     * 
     * The general format is rw://[userid]:[key]@[hostname]/[stationId] though
     * currently only user ID's and keys are used.
     * 
     * @param uri
     *            the uri to parse
     * @return a 2-item array containing User ID and key, or null if the parse
     *         failed
     */
    public static String[] parseUrl(final Uri uri) {
        if(!Rainwave.SCHEME.equals(uri.getScheme())) {
            return null;
        }
        final String userInfo = uri.getUserInfo();
        if(userInfo != null) {
            return userInfo.split("[:]", 2);
        }
        return null;
    }

    public static String getTimeTemplate(Context ctx, long time) {
        long d = time / 86400, h = time / 3600, m = time / 60;
        String template;
        Resources r = ctx.getResources();
        long n;
        if(d > 0) {
            n = d;
            template = r.getString(R.string.template_days);
        }
        else if(h > 0) {
            n = h;
            template = r.getString(R.string.template_hours);
        }
        else if(m > 0) {
            n = m;
            template = r.getString(R.string.template_minutes);
        }
        else {
            n = time;
            template = r.getString(R.string.template_seconds);
        }
        return String.format(template, n);
    }

    public static final int
        USERID_MAX = 10,
        KEY_MAX = 10;

    /** Bundle constants */
    public static final String
        HANDLED_URI = "handled-uri",
        SCHEDULE = "schedule",
        ART = "art";

    public static final String
        RAINWAVE_URL = "http://rainwave.cc/api4",
        SCHEME = "rw";

    public static final URL    DEFAULT_URL;

    static {
        URL tmp;
        try {
            tmp = new URL(RAINWAVE_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not initialize the default URL.", e);
        }
        DEFAULT_URL = tmp;
    }
}

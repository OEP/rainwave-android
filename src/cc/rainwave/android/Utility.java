package cc.rainwave.android;

import cc.rainwave.android.api.types.Song;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

/**
 * A non-class for utility methods.
 */
public final class Utility {
    private Utility() {}

    /**
     * Get a compact string for displaying a time amount.
     * 
     * @param ctx
     *            Context for which to retrieve resources
     * @param seconds
     *            Number of seconds
     * @return A localized string representing a time amount, e.g., "16h" or
     *         "45s" in English
     */
    public static String getCooldownString(Context ctx, long seconds) {
        long d = seconds / 86400, h = seconds / 3600, m = seconds / 60;
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
            n = seconds;
            template = r.getString(R.string.template_seconds);
        }
        return String.format(template, n);
    }

    /**
     * Get a comma-seperated list of song IDs.
     * 
     * @param songs
     *            Array of song objects to join by ID
     * @return For null or 0-item array, the empty string; otherwise, a string
     *         of the song ID's joined by commas
     */
    public static String joinIds(Song songs[]) {
        if(songs == null || songs.length == 0) {
            return "";
        }
        else if(songs.length == 1) {
            return String.valueOf(songs[0].getId());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(songs[0].getId());
        for(int i = 1; i < songs.length; i++) {
            sb.append(",");
            sb.append(songs[i].getId());
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
     *            The uri to parse
     * @return A 2-item array containing User ID and key, or null if the parse
     *         failed
     */
    public static String[] parseUrl(final Uri uri) {
        if (!Rainwave.SCHEME.equals(uri.getScheme())) {
            return null;
        }
        final String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            return userInfo.split("[:]", 2);
        }
        return null;
    }
}

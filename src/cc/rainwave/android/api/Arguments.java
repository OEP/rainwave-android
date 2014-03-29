package cc.rainwave.android.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Arguments extends HashMap<String, String> {
    Arguments(final String[] args) {
        super();
		if(args.length % 2 != 0) {
			throw new IllegalArgumentException("Must have a multiple of two arguments");
		}
        for(int i = 0; i < args.length; i += 2) {
            put(args[i], args[i+1]);
        }
    }

    public String encode() throws UnsupportedEncodingException {
        return encode("UTF-8");
    }

    public String encode(final String codec) throws UnsupportedEncodingException {
		StringBuffer buffer = new StringBuffer();
        Iterator it = entrySet().iterator();

        while(it.hasNext()) {
            Arguments.Entry pair = (Arguments.Entry) it.next();
            final String key = (String) pair.getKey();
            final String value = (String) pair.getValue();
			buffer.append(URLEncoder.encode(key, codec));
			buffer.append('=');
			buffer.append(URLEncoder.encode(value, codec));
            if(it.hasNext()) {
                buffer.append('&');
            }
		}
		return buffer.toString();
    }
}


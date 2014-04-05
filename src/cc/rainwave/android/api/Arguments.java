package cc.rainwave.android.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

public class Arguments extends HashMap<String, String> {
	private static final long serialVersionUID = 7112726141447554961L;

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
        Iterator<Entry<String, String>> it = entrySet().iterator();

        while(it.hasNext()) {
            Arguments.Entry<String, String> pair = it.next();
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


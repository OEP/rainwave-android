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


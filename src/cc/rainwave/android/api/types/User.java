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

package cc.rainwave.android.api.types;

import java.lang.reflect.Type;

import cc.rainwave.android.api.JsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class User {
    /** User's username */
    private String mUsername;

    /** Numeric user ID */
    private int mUserId;

    /** True if user is tuned in. */
    private boolean mTunedIn = false;

    /** Can't instantiate directly. */
    private User() {}

    public boolean getTunedIn() {
        return mTunedIn;
    }

    public int getId() {
        return mUserId;
    }

    public String getUsername() {
        return mUsername;
    }

    public static class Deserializer implements JsonDeserializer<User> {
        @Override
        public User deserialize(
            JsonElement element, Type type,    JsonDeserializationContext ctx
        ) throws JsonParseException {
            final User u = new User();
            u.mUsername = JsonHelper.getString(element, "name");
            u.mUserId = JsonHelper.getInt(element, "id");
            u.mTunedIn = JsonHelper.getBoolean(element, "tuned_in");
            return u;
        }
    }
}

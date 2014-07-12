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

import android.os.Parcel;
import android.os.Parcelable;

public class Station implements Parcelable {
    /** Description of radio station. */
    private String mDescription;

    /** URL to main stream. */
    private String mStream;

    /** Name of radio station. */
    private String mName;

    /** ID of radio station. */
    private int mId;

    /** Can't instantiate directly. */
    private Station() {}

    private Station(Parcel in) {
        mDescription = in.readString();
        mStream = in.readString();
        mName = in.readString();
        mId = in.readInt();
    }

    /**
     * Get the radio station's name.
     * @return the name
     */
    public String getName() {
        return mName;
    }

    /**
     * Get the radio station's description.
     * @return the description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Get the URL to the main stream.
     * @return url to stream
     */
    public String getMainStream() {
        return mStream;
    }

    /**
     * Get the station's ID.
     * @return station id
     */
    public int getId() {
        return mId;
    }

    public String toString() {
        return mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDescription);
        dest.writeString(mStream);
        dest.writeString(mName);
        dest.writeInt(mId);
    }

    public static final Parcelable.Creator<Station> CREATOR
    = new Parcelable.Creator<Station>() {
        @Override
        public Station createFromParcel(Parcel source) {
            return new Station(source);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };

    public static class Deserializer implements JsonDeserializer<Station> {
        @Override
        public Station deserialize(
            JsonElement element, Type type,    JsonDeserializationContext ctx
        ) throws JsonParseException {
            final Station s = new Station();
            s.mDescription = JsonHelper.getString(element, "description");
            s.mStream = JsonHelper.getString(element, "stream");
            s.mName = JsonHelper.getString(element, "name");
            s.mId = JsonHelper.getInt(element, "id");
            return s;
        }
    }
}

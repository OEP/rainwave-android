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

public class Event implements Parcelable {
    private int mId;
    private int mStationId;
    private Song mSongs[];
    private long mEnd;

    /** Can't instantiate directly. */
    private Event() {}

    private Event(Parcel source) {
        Parcelable tmp[] = source.readParcelableArray(Song[].class.getClassLoader());
        mEnd = source.readLong();
        mSongs = new Song[tmp.length];

        for(int i = 0; i < tmp.length; i++) {
            mSongs[i] = (Song) tmp[i];
        }
    }

    public int getId() {
        return mId;
    }

    public int getStationId() {
        return mStationId;
    }

    public int getSongCount() {
        return mSongs.length;
    }

    public Song[] cloneSongs() {
        return mSongs.clone();
    }

    /**
     * Get the currently playing song (for a "current event" only).
     * @return current playing song
     */
    public Song getCurrentSong() {
        return getSong(0);
    }

    public Song getSong(int i) {
        return mSongs[i];
    }

    public long getEnd() {
        return mEnd;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelableArray(mSongs, flags);
        dest.writeLong(mEnd);
    }

    public static final Parcelable.Creator<Event> CREATOR
    = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public static class Deserializer implements JsonDeserializer<Event> {
        @Override
        public Event deserialize(
            JsonElement element, Type type,    JsonDeserializationContext ctx
        ) throws JsonParseException {
            final Event a = new Event();
            a.mId = JsonHelper.getInt(element, "id");
            a.mStationId = JsonHelper.getInt(element, "sid");
            a.mEnd = JsonHelper.getLong(element, "end");
            a.mSongs = ctx.deserialize(JsonHelper.getJsonArray(element, "songs"), Song[].class);
            return a;
        }
    }
}

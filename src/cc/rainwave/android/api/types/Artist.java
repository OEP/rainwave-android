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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Parcel;
import android.os.Parcelable;
import cc.rainwave.android.api.JsonHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class Artist implements Parcelable, Comparable<Artist> {
    /** Artist ID */
    private int mId;

    /** Artist name */
    private String mName;

    /** Maps station IDs to songs they contain. */
    private Map<Integer, List<Song>> mSongs;

    /** Can't instantiate directly. */
    private Artist() {}

    /** Utility constructor for placeholder objects. */
    public Artist(int id, String name) {
        mId = id;
        mName = name;
    }

    private Artist(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    /**
     * Copies the list of songs for the given station ID.
     * @param stationId the integer station id
     * @return an array of songs, or null
     */
    public Song[] cloneSongs(int stationId) {
        List<Song> songList = mSongs.get(stationId);
        if(songList == null) {
            return null;
        }
        return songList.toArray(new Song[songList.size()]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel source) {
            return new Artist(source);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    public static class Deserializer implements JsonDeserializer<Artist> {
        @Override
        public Artist deserialize(
            JsonElement element, Type type,    JsonDeserializationContext ctx
        ) throws JsonParseException {
            final Artist a = new Artist();
            a.mId = JsonHelper.getInt(element, "id");
            a.mName = JsonHelper.getString(element, "name");

            JsonElement all_songs_element = JsonHelper.getChild(element, "all_songs", null);
            if(all_songs_element != null) {
                JsonObject all_songs = JsonHelper.castAsJsonObject(all_songs_element);
                a.mSongs = new HashMap<Integer, List<Song>>();
                for(Entry<String, JsonElement> entry : all_songs.entrySet()) {
                    int stationId = Integer.parseInt(entry.getKey());
                    List<Song> songs = a.mSongs.get(stationId);
                    if(songs == null) {
                        songs = new ArrayList<Song>();
                        a.mSongs.put(stationId, songs);
                    }

                    JsonObject songObject = JsonHelper.castAsJsonObject(entry.getValue());
                    for(Entry<String, JsonElement> albumEntry : songObject.entrySet()) {
                        JsonElement songListElement = albumEntry.getValue();
                        JsonArray songListArray = songListElement.getAsJsonArray();

                        // Loop over Json song list and add to the song list.
                        for(JsonElement songElement : songListArray) {
                            Song song = ctx.deserialize(songElement, Song.class);
                            songs.add(song);
                        }
                    }
                }
            }

            return a;
        }
    }

    @Override
    public int compareTo(Artist another) {
        return mName.compareTo(another.mName);
    }

    public String toString() {
        return mName;
    }
}

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

import android.os.Parcel;
import android.os.Parcelable;
import cc.rainwave.android.api.JsonHelper;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class Album implements Parcelable, Comparable<Album> {
    /** The soonest cooldown time of any songs. */
    private long mLowestCooldown;
    
    /** Community rating of album. */
    private float mRating;
    
    /** User rating of album. */
    private float mUserRating;
    
    /** Partial path to album art. */
    private String mArt;
    
    /** Name of album. */
    private String mName;
    
    /** Album ID. */
    private int mId;
    
    /** Songs in album. */
    private Song mSongs[];
    
    private Album() {
        
    }
    
    private Album(Parcel source) {
        mLowestCooldown = source.readLong();
        mRating = source.readFloat();
        mUserRating = source.readFloat();
        mName = source.readString();
        mArt = source.readString();
        mId = source.readInt();
        final Parcelable tmpSongs[] = source.readParcelableArray(Song[].class.getClassLoader());
        
        mSongs = new Song[tmpSongs.length];
        for(int i = 0; i < tmpSongs.length; i++) {
            mSongs[i] = (Song) tmpSongs[i];
        }
    }
    
    public String getName() {
        return mName;
    }
    
    public String getArt() {
        return mArt;
    }
    
    public float getUserRating() {
        return mUserRating;
    }
    
    public int getId() {
        return mId;
    }
    
    public int getSongCount() {
        return mSongs.length;
    }
    
    public Song getSong(int i) {
        return mSongs[i];
    }
    
    public Song[] cloneSongs() {
        return mSongs.clone();
    }
    
    public float getCommunityRating() {
        return mRating;
    }

    public String toString() {
        return getName();
    }
    
    public boolean isCooling() {
        return getCooldown() > 0;
    }
    
    public long getCooldown() {
        long utc = System.currentTimeMillis() / 1000;
        return mLowestCooldown - utc;
    }

    @Override
    public int compareTo(Album a) {
        return getName().compareTo(a.getName());
    }

    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mLowestCooldown);
        dest.writeFloat(getCommunityRating());
        dest.writeFloat(getUserRating());
        dest.writeString(getName());
        dest.writeString(getArt());
        dest.writeInt(getId());
        dest.writeParcelableArray(mSongs, flags);
    }
    
    public static final Parcelable.Creator<Album> CREATOR
    = new Parcelable.Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public static class Deserializer implements JsonDeserializer<Album> {
        @Override
        public Album deserialize(
            JsonElement element, Type type,    JsonDeserializationContext ctx
        ) throws JsonParseException {
            final Album a = new Album();
            a.mArt = JsonHelper.getString(element, "art", null);
            a.mRating = JsonHelper.getFloat(element, "rating", 0.0f);
            a.mUserRating = JsonHelper.getFloat(element, "rating_user", 0.0f);
            a.mName = JsonHelper.getString(element, "name");
            a.mId = JsonHelper.getInt(element, "id");
            a.mLowestCooldown = JsonHelper.getLong(element, "cool_lowest", 0);
            
            // songs may not always be returned by API
            if(JsonHelper.hasMember(element, "songs")) {
                a.mSongs = ctx.deserialize(JsonHelper.getJsonArray(element, "songs"), Song[].class);
            }
            else {
                a.mSongs = null;
            }
            
            return a;
        }
    }
}

package cc.rainwave.android.api.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RainwaveResponse implements Parcelable {
    private Event mCurrent;
    
    private Event mHistory[], mNext[];
    
    private Error mError;
    
    private RatingResult mRatingResult;
    
    private VoteResult mVoteResult;
    
    private User mUser;
    
    private RainwaveResponse(Parcel source) {
        mCurrent = source.readParcelable(Event.class.getClassLoader());
        
        Parcelable history[] = source.readParcelableArray(Event.class.getClassLoader());
        Parcelable next[] = source.readParcelableArray(Event.class.getClassLoader());
        
        mHistory = new Event[history.length];
        mNext = new Event[next.length];
        
        for(int i = 0; i < history.length; i++) { mHistory[i] = (Event) history[i]; }
        for(int i = 0; i < next.length; i++) { mNext[i] = (Event) next[i]; }
    }
    
    public RainwaveResponse() { }

    public Song getCurrentSong() {
        return mCurrent.song_data[0];
    }
    
    public Song[] getElection() {
    	return mNext[0].song_data;
    }
    
    public boolean hasError() {
        return mError != null;
    }
    
    public boolean isTunedIn() {
    	return mUser != null && mUser.radio_tunedin;
    }
    
    public Error getError() {
        return mError;
    }
    
    public VoteResult getVoteResult() {
    	return mVoteResult;
    }
    
    public RatingResult getRateResult() {
        return mRatingResult;
    }
    
    public void receiveUpdates(RainwaveResponse other) {
    	mUser = (User) update(other.mUser, mUser);
    	mCurrent = (Event) update(other.mCurrent, mCurrent);
    	mHistory = (Event[]) update(other.mHistory, mHistory);
    	mNext = (Event[]) update(other.mNext, mNext);
    	mError = (Error) update(other.mError, mError);
    	mRatingResult = (RatingResult) update(other.mRatingResult, mRatingResult);
    	mVoteResult = (VoteResult) update(other.mVoteResult, mVoteResult);
    }
    
    private Object update(Object old, Object current) {
    	if(old == null) return current;
    	return old;
    }
    
    public void updateSongRatings(RatingResult result) {
        Song s = getCurrentSong();
        s.song_rating_user = result.song_rating;
        s.album_rating_user = result.album_rating;
    }
    
    public static class Deserializer implements JsonDeserializer<RainwaveResponse> {
        private static final String TAG = "ScheduleOrganizer.Deserializer";

        @Override
        public RainwaveResponse deserialize(JsonElement json, Type type,
                JsonDeserializationContext ctx) throws JsonParseException {
            JsonArray mainArray = json.getAsJsonArray();
            
            RainwaveResponse organizer = new RainwaveResponse();
            
            // mainArray should be an array of JsonObjects
            for(int i = 0; i < mainArray.size(); i++) {
                JsonElement scheduleJson = mainArray.get(i);
                JsonObject schedule = scheduleJson.getAsJsonObject();
                
                // The schedule as a JSON should contain one member which is bound
                // to the actual schedule data.
                Set<Map.Entry<String, JsonElement>> members = schedule.entrySet();
                
                // This little doodad just finds the first member inside the
                // object and extracts its name and the data it is bound to.
                Iterator<Entry<String, JsonElement>> it = members.iterator();
                Entry<String,JsonElement> first = it.next();
                String name = first.getKey();
                JsonElement data = first.getValue();
                
                // Hopefully there will only ever be three types of schedules,
                // so we'll just check the name of the member and assign it to
                // the appropriate field.
                
                if(name.compareTo(SCHED_HISTORY) == 0) {
                    organizer.mHistory = (Event[]) ctx.deserialize(data, Event[].class);
                }
                else if(name.compareTo(SCHED_NEXT) == 0) {
                    organizer.mNext = (Event[]) ctx.deserialize(data, Event[].class);
                }
                else if(name.compareTo(SCHED_CURRENT) == 0){
                    organizer.mCurrent = ctx.deserialize(data, Event.class);
                }
                else if(name.compareTo(ERROR) == 0) {
                    organizer.mError = ctx.deserialize(data, Error.class);
                }
                else if(name.compareTo(RATING_RESULT) == 0) {
                    organizer.mRatingResult = ctx.deserialize(data, RatingResult.class);
                }
                else if(name.compareTo(VOTE_RESULT) == 0) {
                	organizer.mVoteResult = ctx.deserialize(data, VoteResult.class);
                }
                else if(name.compareTo(USER) == 0) {
                	organizer.mUser = ctx.deserialize(data, User.class);
                }
            }
            
            return organizer;
        }
        
        public static final String
        	USER = "user",
        	VOTE_RESULT = "vote_result",
            RATING_RESULT = "rate_result",
            ERROR = "error",
            SCHED_CURRENT = "sched_current",
            SCHED_NEXT = "sched_next",
            SCHED_HISTORY = "sched_history";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mCurrent, flags);
    }
    
    public static final Parcelable.Creator<RainwaveResponse> CREATOR
    = new Parcelable.Creator<RainwaveResponse>() {
        @Override
        public RainwaveResponse createFromParcel(Parcel source) {
            return new RainwaveResponse(source);
        }

        @Override
        public RainwaveResponse[] newArray(int size) {
            return new RainwaveResponse[size];
        }
    };
}

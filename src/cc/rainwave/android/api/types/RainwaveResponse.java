package cc.rainwave.android.api.types;

import android.os.Parcel;
import android.os.Parcelable;

public class RainwaveResponse implements Parcelable {
    private Event sched_current;
    
    private Event sched_history[], sched_next[];
    
    private Song requests_all[], requests_user[];
    
    private Error error;
    
    private RatingResult rate_result;
    
    private VoteResult vote_result;
    
    private User user;
    
    private Station stations[];
    
    private RainwaveResponse(Parcel source) {
        sched_current = source.readParcelable(Event.class.getClassLoader());
        
        Parcelable history[] = source.readParcelableArray(Event.class.getClassLoader());
        Parcelable next[] = source.readParcelableArray(Event.class.getClassLoader());
        Parcelable tmpStations[] = source.readParcelableArray(Station.class.getClassLoader());
        
        sched_history = new Event[history.length];
        sched_next = new Event[next.length];
        stations = new Station[tmpStations.length];
        
        for(int i = 0; i < history.length; i++) { sched_history[i] = (Event) history[i]; }
        for(int i = 0; i < next.length; i++) { sched_next[i] = (Event) next[i]; }
        for(int i = 0; i < tmpStations.length; i++) { stations[i] = (Station) tmpStations[i]; }
    }
    
    public RainwaveResponse() { }

    public Song getCurrentSong() {
        return sched_current.song_data[0];
    }
    
    public long getEndTime() {
    	return sched_current.sched_endtime;
    }
    
    public Song[] getElection() {
    	return sched_next[0].song_data;
    }
    
    public int getPastVote() {
    	return vote_result.elec_entry_id;
    }
    
    public boolean hasVoteResult() {
    	return vote_result != null;
    }
    
    public boolean hasError() {
        return error != null;
    }
    
    public boolean isTunedIn() {
    	return user != null && user.radio_tunedin;
    }
    
    public Error getError() {
        return error;
    }
    
    public VoteResult getVoteResult() {
    	return vote_result;
    }
    
    public RatingResult getRateResult() {
        return rate_result;
    }
    
    public Song[] getRequests() {
    	return requests_user;
    }
    
    public Station getStation(int stationId) {
    	for(int i = 0; i < stations.length; i++) {
    		if(stationId == stations[i].id) return stations[i];
    	}
    	return null;
    }
    
    public Station[] getStations() {
    	return stations;
    }
    
    public String getStationName(int id) {
    	Station s = getStation(id);
    	return (s == null) ? null : s.name;
    }
    
    public void setStations(Station[] newStations) {
    	stations = newStations;
    }
    
    public void receiveUpdates(RainwaveResponse other) {
    	user = (User) update(other.user, user);
    	sched_current = (Event) update(other.sched_current, sched_current);
    	sched_history = (Event[]) update(other.sched_history, sched_history);
    	sched_next = (Event[]) update(other.sched_next, sched_next);
    	error = (Error) update(other.error, error);
    	rate_result = (RatingResult) update(other.rate_result, rate_result);
    	vote_result = (VoteResult) update(other.vote_result, vote_result);
    	stations = (Station[]) update(other.stations, stations);
    	requests_all = (Song[]) update(other.requests_all, requests_all);
    	requests_user = (Song[]) update(other.requests_user, requests_user);
    }
    
    private Object update(Object newGuy, Object current) {
    	if(newGuy == null) return current;
    	return newGuy;
    }
    
    public void updateSongRatings(RatingResult result) {
        Song s = getCurrentSong();
        s.song_rating_user = result.song_rating;
        s.album_rating_user = result.album_rating;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(sched_current, flags);
        dest.writeParcelableArray(sched_history, flags);
        dest.writeParcelableArray(sched_next, flags);
        dest.writeParcelableArray(stations, flags);
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

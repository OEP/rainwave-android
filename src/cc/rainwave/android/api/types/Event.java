package cc.rainwave.android.api.types;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class Event {
	public Song song_data[];
	
    /*public static class Deserializer implements JsonDeserializer<Event> {
        private static final String TAG = "Schedule.Deserializer";

        @Override
        public Event deserialize(JsonElement json, Type type,
                JsonDeserializationContext ctx) throws JsonParseException {
            
            Event s = new Event();
            JsonObject o = json.getAsJsonObject();
            JsonElement songJson = o.get("song_data");
            
            s.song_data = ctx.deserialize(songJson, Song[].class);
            
            int i = 0;
            i++;
            
            return s;
        }
    }*/
}

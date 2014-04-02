package cc.rainwave.android.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public class JsonHelper {
	
	public static JsonElement getChild(final JsonElement element, final String name) {
		if(!element.isJsonObject()) {
			throw new JsonParseException(String.format("While fetching '%s': not a JSON object.", name));
		}
		final JsonObject obj = element.getAsJsonObject();
		if(!obj.has(name)) {
			throw new JsonParseException(String.format("No such member: '%s'", name));
		}
		return obj.get(name);
	}
	
	public static JsonPrimitive getPrimitive(final JsonElement element, final String name) {
		final JsonElement child = getChild(element, name);
		if(!child.isJsonPrimitive()) {
			throw new JsonParseException(String.format("Not a JSON primitive: '%s'", name));
		}
		return child.getAsJsonPrimitive();
	}
	
	public static Number getNumber(final JsonElement element, final String name) {
		final JsonPrimitive primitive = getPrimitive(element, name);
		if(!primitive.isNumber()) {
			throw new JsonParseException(String.format("Not a JSON number: '%s'", name));
		}
		return primitive.getAsNumber();
	}
	
	public static float getFloat(final JsonElement element, final String name) {
		return getNumber(element, name).floatValue();
	}
	
	public static int getInt(final JsonElement element, final String name) {
		return getNumber(element, name).intValue();
	}

	public static String getString(final JsonElement element, final String name) {
		final JsonPrimitive primitive = getPrimitive(element, name);
		if(!primitive.isString()) {
			throw new JsonParseException(String.format("Not a string: '%s'", name));
		}
		return primitive.getAsString();
	}
}

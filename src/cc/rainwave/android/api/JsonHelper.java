package cc.rainwave.android.api;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public class JsonHelper {
	
	private static void throwParseError(final String msg) {
		throwParseError(msg, null);
	}
	
	private static void throwParseError(final String msg, final String prefix) {
		String composite = msg;
		if(prefix != null) {
			composite = String.format("%s: %s", prefix, msg);
		}
		throw new JsonParseException(composite);
	}
	
	public static JsonObject castAsJsonObject(final JsonElement element, final String prefix) {
		if(!element.isJsonObject()) {
			throwParseError("Could not cast as JSON object.", prefix);
		}
		return element.getAsJsonObject();
	}
	
	public static JsonPrimitive castAsJsonPrimitive(final JsonElement element, final String prefix) {
		if(!element.isJsonPrimitive()) {
			throwParseError("Could not cast as JSON primitive.", prefix);
		}
		return element.getAsJsonPrimitive();
	}
	
	public static JsonElement getChild(final JsonElement element, final String name) {
		final JsonObject obj = castAsJsonObject(element, String.format("While fetching '%s'", name));
		if(!obj.has(name)) {
			throwParseError(String.format("No such member: '%s'", name));
		}
		return obj.get(name);
	}
	
	public static JsonArray getJsonArray(final JsonElement element, final String name) {
		final JsonElement child = getChild(element, name);
		if(!child.isJsonArray()) {
			throwParseError(String.format("Not a JSON array: '%s'", name));
		}
		return child.getAsJsonArray();
	}
	
	public static JsonPrimitive getPrimitive(final JsonElement element, final String name) {
		final JsonElement child = getChild(element, name);
		if(!child.isJsonPrimitive()) {
			throwParseError(String.format("Not a JSON primitive: '%s'", name));
		}
		return child.getAsJsonPrimitive();
	}
	
	public static Number getNumber(final JsonElement element, final String name) {
		final JsonPrimitive primitive = getPrimitive(element, name);
		if(!primitive.isNumber()) {
			throwParseError(String.format("Not a JSON number: '%s'", name));
		}
		return primitive.getAsNumber();
	}
	
	public static float getFloat(final JsonElement element, final String name) {
		return getNumber(element, name).floatValue();
	}
	
	public static int getInt(final JsonElement element, final String name) {
		return getNumber(element, name).intValue();
	}
	
	public static long getLong(final JsonElement element, final String name) {
		return getNumber(element, name).longValue();
	}
	
	public static boolean hasMember(final JsonElement element, final String name) {
		final JsonObject obj = castAsJsonObject(element, String.format("Checking for member '%s'", name));
		return obj.has(name);
	}
	
	public static boolean isNull(final JsonElement element, final String name) {
		final JsonElement child = getChild(element, name);
		return child.isJsonNull();
	}

	public static String getString(final JsonElement element, final String name) {
		final JsonElement child = getChild(element, name);
		if(child.isJsonNull()) {
			return null;
		}
		final JsonPrimitive primitive = castAsJsonPrimitive(child, String.format("Casting member '%s'", name));
		if(!primitive.isString()) {
			throwParseError(String.format("Not a string: '%s'", name));
		}
		return primitive.getAsString();
	}
	
	public static boolean getBoolean(final JsonElement element, final String name) {
		final JsonPrimitive primitive = getPrimitive(element, name);
		if(!primitive.isBoolean()) {
			throwParseError(String.format("Not a boolean: '%s'", name));
		}
		return primitive.getAsBoolean();
	}
	
	public static boolean getBoolean(final JsonElement element, final String name, boolean defaultValue) {
		if(hasMember(element, name) && !isNull(element, name)) {
			return getBoolean(element, name);
		}
		return defaultValue;
	}
	
	public static long getLong(final JsonElement element, final String name, long defaultValue) {
		if(hasMember(element, name) && !isNull(element, name)) {
			return getLong(element, name);
		}
		return defaultValue;
	}
	
	public static int getInt(final JsonElement element, final String name, int defaultValue) {
		if(hasMember(element, name) && !isNull(element, name)) {
			return getInt(element, name);
		}
		return defaultValue;
	}
	
	public static float getFloat(final JsonElement element, final String name, float defaultValue) {
		if(hasMember(element, name) && !isNull(element, name)) {
			return getFloat(element, name);
		}
		return defaultValue;
	}
	
	public static String getString(final JsonElement element, final String name, final String defaultValue) {
		if(hasMember(element, name)) {
			return getString(element, name);
		}
		return defaultValue;
	}
	
	public static JsonArray getJsonArray(final JsonElement element, final String name, final JsonArray defaultValue) {
		if(hasMember(element, name)) {
			return getJsonArray(element, name);
		}
		return defaultValue;
	}
}

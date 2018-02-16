package solo.utils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

/** */
public class JsonUtils
{
	/** По исходному json формируем карту с данными из json   
	* @param strJson Исходный json
	* @return Карта данных 
	* @throws Exception */
	public final static Map<String, Object> json2Map(final String strJson) throws Exception
	{
		final GsonBuilder oGsonBuilder = new GsonBuilder();
		oGsonBuilder.registerTypeAdapter(Map.class, new NaturalDeserializer());
		oGsonBuilder.registerTypeAdapter(Value.class, new NaturalDeserializer());
	
		final Gson oGson = oGsonBuilder.create();
		final Type oType = new TypeToken<Map<String, Object>>(){}.getType();
		return oGson.fromJson(strJson, oType);
	}

	/** По исходному json формируем карту с данными из json   
	* @param strJson Исходный json
	* @return Карта данных 
	* @throws Exception */
	public final static List<Object> json2List(final String strJson) throws Exception
	{
		final GsonBuilder oGsonBuilder = new GsonBuilder();
		oGsonBuilder.registerTypeAdapter(Map.class, new NaturalDeserializer());
		oGsonBuilder.registerTypeAdapter(List.class, new NaturalDeserializer());
		oGsonBuilder.registerTypeAdapter(Value.class, new NaturalDeserializer());
	
		final Gson oGson = oGsonBuilder.create();
		final Type oType = new TypeToken<List<Object>>(){}.getType();
		return oGson.fromJson(strJson, oType);
	}

	/** По исходному json формируем карту с данными из json   
	* @param strJson Исходный json
	* @return Карта данных 
	* @throws Exception */
	public final static <T extends Object> T fromJson(final String strJson, final Class<T> oClass)
	{
		final GsonBuilder oGsonBuilder = new GsonBuilder();
		oGsonBuilder.registerTypeAdapter(Map.class, new NaturalDeserializer());
		oGsonBuilder.registerTypeAdapter(List.class, new NaturalDeserializer());
		oGsonBuilder.registerTypeAdapter(Value.class, new NaturalDeserializer());

		final Gson oGson = oGsonBuilder.create();
		return oClass.cast(oGson.fromJson(strJson, oClass));
	}

	/** По исходному json формируем карту с данными из json   
	* @param strJson Исходный json
	* @return Карта данных 
	* @throws Exception */
	public final static String toJson(final Object oValue)
	{
		final GsonBuilder oGsonBuilder = new GsonBuilder();
		final Gson oGson = oGsonBuilder.create();
		return oGson.toJson(oValue);
	}

	/** По исходному json формируем карту с данными из json   
	* @param strJson Исходный json
	* @return Карта данных 
	* @throws Exception */
	public final static String formatJson(final String strJson)
	{
		return strJson.replace("\\r", "\r").replace("\\n", "\n").replace("\\t", "\t").replace("{", "\r{").replace(",\"", ",\r\t\"");
	}
}

class Value extends Object
{
}

class NaturalDeserializer implements JsonDeserializer<Object> 
{
	@Override
	public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
	{
		 if(json.isJsonNull()) 
			 return null;
	   
		 if(json.isJsonPrimitive()) 
			 return handlePrimitive(json.getAsJsonPrimitive());

		 if(json.isJsonArray()) 
			 return handleArray(json.getAsJsonArray(), context);

		 return handleObject(json.getAsJsonObject(), context);
	}
	 
	private Object handlePrimitive(JsonPrimitive json) 
	{
		if(json.isBoolean())
			return json.getAsBoolean();
		   
		if(json.isString())
			return json.getAsString();
	
		 BigDecimal bigDec = json.getAsBigDecimal();
		 try 
		 {
			 bigDec.toBigIntegerExact();
			 try 
			 { 
				 return bigDec.intValueExact();
			 }
			 catch(ArithmeticException e) {}

			 return bigDec.longValue();
		 } 
		 catch(ArithmeticException e) {}
			   
		 return bigDec.doubleValue();
	 }
	 
	 private Object handleArray(JsonArray json, JsonDeserializationContext context) 
	 {
		 final List<Object> array = new LinkedList<Object>();
		 for(int i = 0; i < json.size(); i++)
		 array.add(context.deserialize(json.get(i), Value.class));
		   
		 return array;
	 }
	 
	 private Object handleObject(JsonObject json, JsonDeserializationContext context) 
	 {
		 final Map<String, Object> map = new HashMap<String, Object>();
		 for(Map.Entry<String, JsonElement> entry : json.entrySet())
		 map.put(entry.getKey(), context.deserialize(entry.getValue(), Value.class));
		   
		 return map;
	 }
}

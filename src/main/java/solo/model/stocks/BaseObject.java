package solo.model.stocks;

import solo.utils.JsonUtils;

public class BaseObject
{
	public String toJson()
	{
		return JsonUtils.toJson(this);
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		return toJson().replace("\\r", "\r")
						.replace("\\n", "\n")
						.replace("\\t", "\t")
						.replace("{", "\r{")
						.replace("},\"", "},\r\"")
						.replace("],\"", "],\r\"")
						; 
		//.replace(",\"", ",\r\t\"");
//		return JsonUtils.formatJson(toJson());
	}
}

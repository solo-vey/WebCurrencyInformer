package solo.model.stocks.item;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import solo.CurrencyInformer;
import solo.model.stocks.exchange.IStockExchange;
import ua.lz.ep.utils.JsonUtils;
import ua.lz.ep.utils.ResourceUtils;

public class Rules
{
	final protected IStockExchange m_oStockExchange;
	final protected Map<Integer, IRule> m_oRules = new HashMap<Integer, IRule>();

	protected Integer m_nLastRuleID = 0;
	
	public Rules(final IStockExchange oStockExchange)
	{
		m_oStockExchange = oStockExchange;
	}
	
	public void addRule(final IRule oRule)
	{
		if (null == oRule)
			return;
		
		m_oRules.put(m_nLastRuleID, oRule);
		System.err.printf("Add rule : " + oRule + "\r\n");
		m_nLastRuleID++;
		save();
	}
	
	public void removeRule(final Integer nRuleID)
	{
		m_oRules.remove(nRuleID);
		save();
	}
	
	public Map<Integer, IRule> getRules()
	{
		return m_oRules;
	}
	
	private void save()
	{
		try
		{
			final String strJson = JsonUtils.toJson(m_oRules);
			final String strStockEventsFileName = getFileName();
			FileUtils.writeStringToFile(new File(strStockEventsFileName), strJson);
		}
		catch (IOException e){	}
	}

	protected void load()
	{
		try
		{
			final String strStockEventsFileName = getFileName(); 
			String strJson = new String(Files.readAllBytes((new File(strStockEventsFileName)).toPath()));
			final GsonBuilder oGsonBuilder = new GsonBuilder();
	
			final Gson oGson = oGsonBuilder.create();
			final Type oType = new TypeToken<HashMap<Integer, IRule>>(){}.getType();
			final HashMap<Integer, IRule> oRules = oGson.fromJson(strJson, oType);
			m_oRules.clear();
			m_oRules.putAll(oRules);
		}
		catch (IOException e) { 		}
	}

	/**
	 * @return
	 */
	String getFileName()
	{
		final String strStockEventsFileName = ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_oStockExchange.getStockName() + "\\rules.json";
		return strStockEventsFileName;
	}
}
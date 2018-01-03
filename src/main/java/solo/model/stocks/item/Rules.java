package solo.model.stocks.item;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import solo.CurrencyInformer;
import solo.model.stocks.exchange.IStockExchange;
import ua.lz.ep.utils.ResourceUtils;

public class Rules
{
	final protected IStockExchange m_oStockExchange;
	final protected Map<Integer, IRule> m_oRules = new HashMap<Integer, IRule>();

	protected Integer m_nLastRuleID = 0;
	
	public Rules(final IStockExchange oStockExchange)
	{
		m_oStockExchange = oStockExchange;
		load();
		for(final Integer nRuleID : m_oRules.keySet())
			m_nLastRuleID = (m_nLastRuleID <= nRuleID ? nRuleID + 1 : m_nLastRuleID);
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
		final IRule oRule = m_oRules.get(nRuleID);
		if (null == oRule)
			return;
		
		oRule.remove();
		m_oRules.remove(nRuleID);
		save();
	}
	
	public void removeRule(final IRule oRule)
	{
		final Integer nRuleID = getRuleID(oRule);
		if (null != nRuleID)
			removeRule(nRuleID);
	}
	
	public Integer getRuleID(final IRule oRule)
	{
		for(final Entry<Integer, IRule> oRuleInfo : m_oRules.entrySet())
		{
			if (oRuleInfo.getValue().equals(oRule))
				return oRuleInfo.getKey();
		}
		
		return null;
	}
	
	public Map<Integer, IRule> getRules()
	{
		return m_oRules;
	}
	
	public void save()
	{
		final String strStockEventsFileName = getFileName();

		try 
		{
	         final FileOutputStream oFileStream = new FileOutputStream(strStockEventsFileName);
	         final ObjectOutputStream oStream = new ObjectOutputStream(oFileStream);
	         oStream.writeObject(m_oRules);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (IOException e) 
		{
			System.err.printf("Save rules exception : " + e.getMessage());
		}			
	}

	@SuppressWarnings("unchecked")
	public void load()
	{
		final String strStockEventsFileName = getFileName(); 
		try 
		{
	         final FileInputStream oFileStream = new FileInputStream(strStockEventsFileName);
	         final ObjectInputStream oStream = new ObjectInputStream(oFileStream);
	         final Map<Integer, IRule> oRules = (Map<Integer, IRule>) oStream.readObject();
	         m_oRules.clear();
	         m_oRules.putAll(oRules);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (final Exception e) 
		{
			System.err.printf("Load rules exception : " + e.getMessage() + "\r\n");
	    }			
	}

	String getFileName()
	{
		final String strStockEventsFileName = ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_oStockExchange.getStockName() + "\\rules.ser";
		return strStockEventsFileName;
	}
}

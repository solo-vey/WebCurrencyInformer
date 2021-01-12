package solo.model.stocks.item;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import solo.CurrencyInformer;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.utils.ResourceUtils;
import solo.utils.TraceUtils;

public class Rules
{
	protected final IStockExchange m_oStockExchange;
	protected final Map<Integer, IRule> m_oRules = new HashMap<>();

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
		oRule.setID(m_nLastRuleID);
		if (!ManagerUtils.isTestObject(oRule))
			TraceUtils.writeTrace("Add rule : " + oRule);
		
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
	
	public List<Entry<Integer, IRule>> getRules(final RateInfo oRateInfo)
	{
		final List<Entry<Integer, IRule>> oResult = new LinkedList<Entry<Integer, IRule>>();
		for(final Entry<Integer, IRule> oRuleInfo : m_oRules.entrySet())
		{
			final IRule oRule = oRuleInfo.getValue();
			final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRule);
			if (null != oTradeTask && !oTradeTask.getTradeControler().equals(ITradeControler.NULL))
				continue;
			
			if (oRule.getRateInfo().equals(oRateInfo))
				oResult.add(oRuleInfo);
		}
		return oResult;
	}
	
	public int getNextRuleID()
	{
		return m_nLastRuleID;
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
			TraceUtils.writeError("Save rules exception", e);
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
	         final Map<Integer, IRule> oRules = (Map<Integer, IRule>)oStream.readObject();
	         m_oRules.clear();
	         m_oRules.putAll(oRules);
	         
	         for(final Entry<Integer, IRule> oRule : m_oRules.entrySet())
	        	 oRule.getValue().setID(oRule.getKey());
	         
	         oStream.close();
	         oFileStream.close();
		} 
		catch (final Exception e) 
		{
			TraceUtils.writeError("Load rules exception", e);
			save();
	    }			
	}

	String getFileName()
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_oStockExchange.getStockName() + "\\rules.ser";
	}
}

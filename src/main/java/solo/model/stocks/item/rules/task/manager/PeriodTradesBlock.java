package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.rules.task.trade.TaskTrade;

public class PeriodTradesBlock implements Serializable
{
	private static final long serialVersionUID = 7340981230262177314L;
	
	public LinkedHashMap<Integer, CurrencyTradesBlock> m_oPeriodTrades = new LinkedHashMap<Integer, CurrencyTradesBlock>();
	public int nLastUsePeriod = -1;
	
	public void addTrade(final Integer nPeriod, final TaskTrade oTaskTrade)
	{
		if (nLastUsePeriod != nPeriod && m_oPeriodTrades.containsKey(nPeriod))
		{
			final List<Integer> aRemove = new LinkedList<Integer>();
			for(final Integer nKey : m_oPeriodTrades.keySet())
			{
				aRemove.add(nKey);
				if (nKey == nPeriod)
					break;
			}
			for(final Integer nRemovePeriod : aRemove)
				m_oPeriodTrades.remove(nRemovePeriod);
		}
		
		if (!m_oPeriodTrades.containsKey(nPeriod))
			m_oPeriodTrades.put(nPeriod, new CurrencyTradesBlock());
		
		m_oPeriodTrades.get(nPeriod).addTrade(oTaskTrade);
		nLastUsePeriod = nPeriod;
	}
	
	@Override public String toString()
	{
		String strResult = StringUtils.EMPTY;
		for(final Entry<Integer, CurrencyTradesBlock> oTradesInfo : m_oPeriodTrades.entrySet())
			strResult += oTradesInfo.getKey() + " - " + oTradesInfo.getValue().toString().replace("\r\n", "\r\n      ") + "\r\n";
		return strResult;
	}
}

package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeInfo;

public class RatePeriodTradesBlock implements Serializable
{
	private static final long serialVersionUID = 7340981233362177314L;
	
	protected LinkedHashMap<Integer, RateTradesBlock> m_oPeriodTrades = new LinkedHashMap<Integer, RateTradesBlock>();
	protected int nLastUsePeriod = -1;
	
	public void addTrade(final Integer nPeriod, final TaskTrade oTaskTrade)
	{
		addTrade(nPeriod, oTaskTrade.getTradeInfo());
	}
	
	public Map<Integer, RateTradesBlock> getPeriods()
	{
		return m_oPeriodTrades;
	}
	
	public void addTrade(final Integer nPeriod, final TradeInfo oTradeInfo)
	{
		synchronized(this)
		{
			if (nLastUsePeriod != nPeriod && m_oPeriodTrades.containsKey(nPeriod))
			{
				final List<Integer> aRemove = new LinkedList<Integer>();
				for(final Integer nKey : m_oPeriodTrades.keySet())
				{
					aRemove.add(nKey);
					if (nKey.equals(nPeriod))
						break;
				}
				for(final Integer nRemovePeriod : aRemove)
					m_oPeriodTrades.remove(nRemovePeriod);
			}
			
			if (!m_oPeriodTrades.containsKey(nPeriod))
				m_oPeriodTrades.put(nPeriod, new RateTradesBlock());
			
			m_oPeriodTrades.get(nPeriod).addTrade(oTradeInfo);
			nLastUsePeriod = nPeriod;
		}
	}
	
	@Override public String toString()
	{
		String strResult = StringUtils.EMPTY;
		for(final Entry<Integer, RateTradesBlock> oTradesInfo : m_oPeriodTrades.entrySet())
			strResult += "[" + oTradesInfo.getKey() + "]" + oTradesInfo.getValue().toString().replace("\r\n", "\r\n") + "\r\n";
		return strResult;
	}
}

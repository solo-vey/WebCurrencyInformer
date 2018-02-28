package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeInfo;

public class RateTradesBlock implements Serializable
{
	private static final long serialVersionUID = 7340771410262177314L;
	
	protected Map<RateInfo, TradesBlock> m_oRateInfoTrades = new HashMap<RateInfo, TradesBlock>();
	
	public void addTrade(final TaskTrade oTaskTrade)
	{
		addTrade(oTaskTrade.getTradeInfo());
	}
	
	public Map<RateInfo, TradesBlock> getRateTrades()
	{
		return m_oRateInfoTrades;
	}
	
	public void addTrade(final RateInfo oRateInfo, final TradesBlock oTradesBlock)
	{
		if (!getRateTrades().containsKey(oRateInfo))
			getRateTrades().put(oRateInfo, new TradesBlock());
		getRateTrades().get(oRateInfo).addTrade(oTradesBlock);
	}

	public void addTrade(final TradeInfo oTradeInfo)
	{
		final RateInfo oRateInfo = oTradeInfo.getRateInfo();
		if (!getRateTrades().containsKey(oRateInfo))
			getRateTrades().put(oRateInfo, new TradesBlock());
		getRateTrades().get(oRateInfo).addTrade(oTradeInfo);
	}
	
	@Override public String toString()
	{
		String strResult = StringUtils.EMPTY;
		final TreeMap<BigDecimal, Entry<RateInfo, TradesBlock>> aSorted = new TreeMap<BigDecimal, Entry<RateInfo, TradesBlock>>();
		for(final Entry<RateInfo, TradesBlock> oTradesInfo : getRateTrades().entrySet())
			aSorted.put(oTradesInfo.getValue().getPercent(), oTradesInfo);
		
		for(final Entry<RateInfo, TradesBlock> oTradesInfo : aSorted.values())
			strResult = oTradesInfo.getKey() + " : " + oTradesInfo.getValue() + "\r\n" + strResult;
		return strResult;
	}
}

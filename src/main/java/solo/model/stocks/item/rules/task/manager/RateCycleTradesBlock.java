package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeInfo;

public class RateCycleTradesBlock implements Serializable
{
	private static final long serialVersionUID = 7340981411152177314L;
	
	public RatePeriodTradesBlock m_oCycleTrades = new RatePeriodTradesBlock();
	
	public void addTrade(final Integer nPeriod, final TaskTrade oTaskTrade)
	{
		addTrade(nPeriod, oTaskTrade.getTradeInfo());
	}
	
	public void addTrade(final Integer nPeriod, final TradeInfo oTradeInfo)
	{
		m_oCycleTrades.addTrade(nPeriod, oTradeInfo);
	}
	
	public Map<Integer, RateTradesBlock> getPeriods()
	{
		return m_oCycleTrades.getPeriods();
	}
	
	public RateTradesBlock getTotal()
	{
		final RateTradesBlock oTotalTradesBlock = new RateTradesBlock();
		for(final RateTradesBlock oCurrencyTradesBlock : m_oCycleTrades.getPeriods().values())
		{
			for(final Entry<RateInfo, TradesBlock> oTradesBlockInfo : oCurrencyTradesBlock.getRateTrades().entrySet())
				oTotalTradesBlock.addTrade(oTradesBlockInfo.getKey(), oTradesBlockInfo.getValue());
		}
		
		return oTotalTradesBlock;
	}
	
	@Override public String toString()
	{
		return getTotal().toString();
	}
}

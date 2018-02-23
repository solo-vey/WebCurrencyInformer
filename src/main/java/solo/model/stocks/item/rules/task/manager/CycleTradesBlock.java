package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.util.Map.Entry;

import solo.model.currency.Currency;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeInfo;

public class CycleTradesBlock implements Serializable
{
	private static final long serialVersionUID = 7340981410552177314L;
	
	public PeriodTradesBlock m_oCycleTrades = new PeriodTradesBlock();
	
	public void addTrade(final Integer nPeriod, final TaskTrade oTaskTrade)
	{
		addTrade(nPeriod, oTaskTrade.getTradeInfo());
	}
	
	public void addTrade(final Integer nPeriod, final TradeInfo oTradeInfo)
	{
		m_oCycleTrades.addTrade(nPeriod, oTradeInfo);
	}
	
	public CurrencyTradesBlock getTotal()
	{
		final CurrencyTradesBlock oTotalTradesBlock = new CurrencyTradesBlock();
		for(final CurrencyTradesBlock oCurrencyTradesBlock : m_oCycleTrades.getPeriods().values())
		{
			for(final Entry<Currency, TradesBlock> oTradesBlockInfo : oCurrencyTradesBlock.getCurrencyTrades().entrySet())
				oTotalTradesBlock.addTrade(oTradesBlockInfo.getKey(), oTradesBlockInfo.getValue());
		}
		
		return oTotalTradesBlock;
	}
	
	@Override public String toString()
	{
		return getTotal().toString();
	}
}

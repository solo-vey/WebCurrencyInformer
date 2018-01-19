package solo.model.stocks.item.rules.task.strategy.trade;

import java.util.List;

import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradesInfo;

public class ReverseTradeStrategy extends SimpleTradeStrategy
{
	public static final String NAME = "ReverseTrade";

	@Override public String getName()
	{
		return NAME;
	}	
	
	public void checkTrade(final ITradeTask oTaskTrade, final int nMaxTrades, final List<ITradeTask> aTaskTrades, final TradesInfo oTradesInfo)
	{
	}
	
	public void checkTrades(final List<ITradeTask> aTaskTrades, final TradesInfo oTradesInfo)
	{
	}
}

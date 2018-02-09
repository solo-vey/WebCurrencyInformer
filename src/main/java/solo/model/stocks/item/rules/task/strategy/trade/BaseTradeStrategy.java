package solo.model.stocks.item.rules.task.strategy.trade;

import java.util.List;

import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeControler;

public class BaseTradeStrategy implements ITradeStrategy
{
	private static final long serialVersionUID = 6260569733645147000L;
	
	public static final String NAME = "BaseTrade";

	@Override public String getName()
	{
		return NAME;
	}	
	
	public boolean checkTrade(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		return false;
	}
	
	public void checkTrades(final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		oTradeControler.getTradesInfo().updateOrderInfo(aTaskTrades);
	}
	
	public boolean isCreateNewTrade(final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		final int nMaxTrades = oTradeControler.getMaxTrades();
		return (nMaxTrades < aTaskTrades.size());
	}
	
	public void startNewTrade(final ITradeTask oTaskTrade, final TradeControler oTradeControler)
	{
	}
}

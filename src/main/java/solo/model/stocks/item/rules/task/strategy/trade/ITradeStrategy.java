package solo.model.stocks.item.rules.task.strategy.trade;

import java.util.List;

import solo.model.stocks.item.rules.task.strategy.IStrategy;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeControler;

public interface ITradeStrategy extends IStrategy
{
	boolean checkTrade(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler);
	void checkTrades(final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler);
	boolean isCreateNewTrade(final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler);
	void startNewTrade(final ITradeTask oTaskTrade, final TradeControler oTradeControler);
}

package solo.model.stocks.item.rules.task.strategy.trade;

import java.util.List;

import solo.model.stocks.item.rules.task.strategy.IStrategy;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradesInfo;

public interface ITradeStrategy extends IStrategy
{
	void checkTrade(final ITradeTask oTaskTrade, final int nMaxTrades, final List<ITradeTask> aTaskTrades, final TradesInfo oTradesInfo);
	void checkTrades(final List<ITradeTask> aTaskTrades, final TradesInfo oTradesInfo);
	boolean isCreateNewTrade(final List<ITradeTask> aTaskTrades, final TradesInfo oTradesInfo);
}

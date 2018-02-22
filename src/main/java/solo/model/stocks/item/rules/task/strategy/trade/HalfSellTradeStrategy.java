package solo.model.stocks.item.rules.task.strategy.trade;

import java.util.List;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.strategy.HalfSellStrategy;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.worker.WorkerFactory;

public class HalfSellTradeStrategy extends BaseTradeStrategy
{
	private static final long serialVersionUID = 6260569777745147775L;
	
	public static final String NAME = "HalfSellTrade";

	@Override public String getName()
	{
		return NAME;
	}	

	public boolean checkTrade(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		stopBuy(oTaskTrade);
		setHalfSellStrategy(oTaskTrade);
		return false;
	}
	
	protected void stopBuy(final ITradeTask oTaskTrade)
	{
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
		if (!OrderSide.BUY.equals(oOrder.getSide()))
			return;
		
		final RateInfo oRateInfo = oTaskTrade.getTradeInfo().getRateInfo();
		final Order oGetOrder = WorkerFactory.getStockSource(oTaskTrade).getOrder(oOrder.getId(), oRateInfo);
		if (oGetOrder.isDone() || oGetOrder.isCanceled() || oGetOrder.isError() || oGetOrder.isException() || oGetOrder.isNull())
			return;
		
		removeBuyOrder(oTaskTrade, oGetOrder, "HalfSellTradeStrategy.stopBuy.");
	}

	protected void setHalfSellStrategy(final ITradeTask oTaskTrade)
	{
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
		if (!OrderSide.SELL.equals(oOrder.getSide()))
			return;
		
		oTaskTrade.getTradeInfo().setSellStrategy(StrategyFactory.getSellStrategy(HalfSellStrategy.NAME), "HalfSellTradeStrategy.setHalfSellStrategy.");		
	}
}

package solo.model.stocks.item.rules.task.strategy.trade;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.source.IStockSource;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;

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
		return (nMaxTrades > aTaskTrades.size());
	}
	
	public void startNewTrade(final ITradeTask oTaskTrade, final TradeControler oTradeControler)
	{
	}
	
	protected void removeBuyOrder(final ITradeTask oTaskTrade, final Order oGetOrder, final String strMessagePrefix)
	{
		final IStockSource oStockSource = WorkerFactory.getStockSource(oTaskTrade);
		final Order oRemoveOrder = TradeUtils.removeOrder(oGetOrder, oTaskTrade.getTradeInfo().getRateInfo(), oStockSource);
		if (oRemoveOrder.isException())
			return;
		
		oTaskTrade.getTradeInfo().getHistory().addToHistory(strMessagePrefix + " Remove order [" + oGetOrder.getId() + "] [" + oGetOrder.getInfoShort() + "].");
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, "Remove order [" + oGetOrder.getId() + "] [" + oGetOrder.getInfoShort() + "].");
		
		if (OrderSide.BUY.equals(oRemoveOrder.getSide()) && null != oRemoveOrder.getVolume())
		{
			final BigDecimal nDeltaBuyVolume = oTaskTrade.getTradeInfo().getNeedBoughtVolume().add(oRemoveOrder.getVolume().negate());
			if (nDeltaBuyVolume.compareTo(BigDecimal.ZERO) > 0)
				oTaskTrade.getTradeInfo().getHistory().addToHistory(strMessagePrefix + " nDeltaBuyVolume on cancel volume [" + nDeltaBuyVolume + "]. Remove order " + oRemoveOrder.getInfoShort());
		}
		oTaskTrade.updateOrderTradeInfo(oRemoveOrder);
	}

	protected void removeSellOrder(final ITradeTask oTaskTrade, final Order oGetOrder, final String strMessagePrefix)
	{
		final IStockSource oStockSource = WorkerFactory.getStockSource(oTaskTrade);
		final Order oRemoveOrder = TradeUtils.removeOrder(oGetOrder, oTaskTrade.getTradeInfo().getRateInfo(), oStockSource);
		if (oRemoveOrder.isException())
			return;
		
		oTaskTrade.getTradeInfo().getHistory().addToHistory(strMessagePrefix + " Remove order [" + oGetOrder.getId() + "] [" + oGetOrder.getInfoShort() + "].");
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, "Remove order [" + oGetOrder.getId() + "] [" + oGetOrder.getInfoShort() + "].");
	
		if (OrderSide.SELL.equals(oRemoveOrder.getSide()) && null != oRemoveOrder.getVolume())
		{
			final BigDecimal nDeltaSellVolume = oTaskTrade.getTradeInfo().getNeedSellVolume().add(oRemoveOrder.getVolume().negate());
			if (nDeltaSellVolume.compareTo(BigDecimal.ZERO) > 0)
				oTaskTrade.getTradeInfo().getHistory().addToHistory(strMessagePrefix + " nDeltaSellVolume on cancel volume [" + nDeltaSellVolume + "]. Remove order " + oRemoveOrder.getInfoShort());
		}
		oTaskTrade.updateOrderTradeInfo(oRemoveOrder);
	}
}

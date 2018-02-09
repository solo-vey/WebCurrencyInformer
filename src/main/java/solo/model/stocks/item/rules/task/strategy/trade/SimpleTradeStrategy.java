package solo.model.stocks.item.rules.task.strategy.trade;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;

public class SimpleTradeStrategy extends BaseTradeStrategy
{
	private static final long serialVersionUID = 6260569733645147335L;
	
	public static final String NAME = "SimpleTrade";

	@Override public String getName()
	{
		return NAME;
	}	
	
	public boolean checkTrade(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		removeBuyIfSmall(oTaskTrade, oTradeControler);
		removeSellIfSmall(oTaskTrade, oTradeControler);
		return false;
	}
	
	protected void removeBuyIfSmall(final ITradeTask oTaskTrade, final TradeControler oTradeControler)
	{
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
		if (!OrderSide.BUY.equals(oOrder.getSide()))
			return;
		
		final RateInfo oRateInfo = oTaskTrade.getTradeInfo().getRateInfo();
		final Order oGetOrder = WorkerFactory.getStockSource().getOrder(oOrder.getId(), oRateInfo);
		if (oGetOrder.isDone() || oGetOrder.isCanceled() || oGetOrder.isError() || oGetOrder.isException() || oGetOrder.isNull())
			return;
		
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oRateInfo);
		if (oGetOrder.getVolume().compareTo(nMinTradeVolume) > 0)
			return;
		
		final BigDecimal nFreeVolume = oTradeControler.getTradesInfo().getVolume();
		if (nFreeVolume.compareTo(nMinTradeVolume) < 0)
			return;
		
		final Date oFithteenMinutesDateCreate = DateUtils.addMinutes(new Date(), -1); 
	    if (null == oGetOrder.getCreated() || oGetOrder.getCreated().after(oFithteenMinutesDateCreate))
	    	return;
		
		final Order oRemoveOrder = TradeUtils.removeOrder(oGetOrder, oTaskTrade.getTradeInfo().getRateInfo());
		if (oRemoveOrder.isException())
			return;
		
		oTaskTrade.getTradeInfo().getHistory().addToHistory("SimpleTradeStrategy.removeBuyIfSmall Remove order [" + oGetOrder.getId() + "] [" + oGetOrder.getInfoShort() + "].");
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, "Remove order [" + oGetOrder.getId() + "] [" + oGetOrder.getInfoShort() + "].");
		
		if (OrderSide.BUY.equals(oRemoveOrder.getSide()) && null != oRemoveOrder.getVolume())
		{
			final BigDecimal nDeltaBuyVolume = oTaskTrade.getTradeInfo().getNeedBoughtVolume().add(oRemoveOrder.getVolume().negate());
			if (nDeltaBuyVolume.compareTo(BigDecimal.ZERO) > 0)
				oTaskTrade.getTradeInfo().getHistory().addToHistory("SimpleTradeStrategy.removeBuyIfSmall. nDeltaBuyVolume on cancel volume [" + nDeltaBuyVolume + "]. Remove order " + oRemoveOrder.getInfoShort());
		}
		oTaskTrade.updateOrderTradeInfo(oRemoveOrder);
	}
	
	protected void removeSellIfSmall(final ITradeTask oTaskTrade, final TradeControler oTradeControler)
	{
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
		if (!OrderSide.SELL.equals(oOrder.getSide()))
			return;
		
		final RateInfo oRateInfo = oTaskTrade.getTradeInfo().getRateInfo();
		final Order oGetOrder = WorkerFactory.getStockSource().getOrder(oOrder.getId(), oRateInfo);
		if (oGetOrder.isDone() || oGetOrder.isCanceled() || oGetOrder.isError() || oGetOrder.isException() || oGetOrder.isNull())
			return;
		
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oRateInfo);
		if (oGetOrder.getVolume().compareTo(nMinTradeVolume) > 0)
			return;
		
		final BigDecimal nFreeSum = oTradeControler.getTradesInfo().getSum();
		final BigDecimal oMinTradeSum = TradeUtils.getMinTradeSum(oRateInfo); 
		if (nFreeSum.compareTo(oMinTradeSum) < 0)
			return;
		
		final Date oFithteenMinutesDateCreate = DateUtils.addMinutes(new Date(), -1); 
	    if (null == oGetOrder.getCreated() || oGetOrder.getCreated().after(oFithteenMinutesDateCreate))
	    	return;
		
		final Order oRemoveOrder = TradeUtils.removeOrder(oGetOrder, oTaskTrade.getTradeInfo().getRateInfo());
		if (oRemoveOrder.isException())
			return;
		
		oTaskTrade.getTradeInfo().getHistory().addToHistory("SimpleTradeStrategy.removeSellIfSmall Remove order [" + oGetOrder.getId() + "] [" + oGetOrder.getInfoShort() + "].");
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, "Remove order [" + oGetOrder.getId() + "] [" + oGetOrder.getInfoShort() + "].");
	
		if (OrderSide.SELL.equals(oRemoveOrder.getSide()) && null != oRemoveOrder.getVolume())
		{
			final BigDecimal nDeltaSellVolume = oTaskTrade.getTradeInfo().getNeedSellVolume().add(oRemoveOrder.getVolume().negate());
			if (nDeltaSellVolume.compareTo(BigDecimal.ZERO) > 0)
				oTaskTrade.getTradeInfo().getHistory().addToHistory("SimpleTradeStrategy.removeSellIfSmall. nDeltaBuyVolume on cancel volume [" + nDeltaSellVolume + "]. Remove order " + oRemoveOrder.getInfoShort());
		}
		oTaskTrade.updateOrderTradeInfo(oRemoveOrder);
	}
	
	public boolean isCreateNewTrade(final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		final int nMaxTrades = oTradeControler.getMaxTrades();
		if (aTaskTrades.size() >= nMaxTrades)
			return false;
		
//		final TradesInfo oTradesInfo = oTradeControler.getTradesInfo();
//		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
//		final Candlestick oCandlestick = oStockCandlestick.get(oTradesInfo.getRateInfo());
		//if (!oCandlestick.isLongFall())
			return !getIsOrderSidePrecent(aTaskTrades, OrderSide.BUY);
		
		//oTradesInfo.setCurrentState("Wait buy. Trand - " + oCandlestick.getType());
		//return false;
	}
	
	public static boolean getIsOrderSidePrecent(final List<ITradeTask> aTaskTrades, final OrderSide oOrderSide)
	{
		boolean bIsPrecent = false;
		for(final ITradeTask oTaskTrade : aTaskTrades)
			bIsPrecent |= oTaskTrade.getTradeInfo().getTaskSide().equals(oOrderSide);
		
		return bIsPrecent;
	}
}

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

public class SimpleTradeStrategy extends BaseTradeStrategy
{
	private static final long serialVersionUID = 6260569733645147335L;
	
	public static final String NAME = "SimpleTrade";

	@Override public String getName()
	{
		return NAME;
	}	
	
	@Override public boolean checkTrade(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
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
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oRateInfo);
		if (oOrder.getVolume().compareTo(nMinTradeVolume) > 0)
			return;
		
		final Order oGetOrder = WorkerFactory.getStockSource(oTaskTrade).getOrder(oOrder.getId(), oRateInfo);
		if (oGetOrder.isDone() || oGetOrder.isCanceled() || oGetOrder.isError() || oGetOrder.isException() || oGetOrder.isNull())
			return;	
		
		if (oGetOrder.getVolume().compareTo(nMinTradeVolume) > 0)
			return;
		
		final BigDecimal nFreeVolume = oTradeControler.getTradesInfo().getVolume();
		if (nFreeVolume.compareTo(nMinTradeVolume) < 0)
			return;
		
		final Date oFithteenMinutesDateCreate = DateUtils.addMinutes(new Date(), -1); 
	    if (null == oGetOrder.getCreated() || oGetOrder.getCreated().after(oFithteenMinutesDateCreate))
	    	return;
		
		removeBuyOrder(oTaskTrade, oGetOrder, "SimpleTradeStrategy.removeBuyIfSmall.");
	}
	
	protected void removeSellIfSmall(final ITradeTask oTaskTrade, final TradeControler oTradeControler)
	{
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
		if (!OrderSide.SELL.equals(oOrder.getSide()))
			return;
		
		final RateInfo oRateInfo = oTaskTrade.getTradeInfo().getRateInfo();
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oRateInfo);
		if (oOrder.getVolume().compareTo(nMinTradeVolume) > 0)
			return;

		final Order oGetOrder = WorkerFactory.getStockSource(oTaskTrade).getOrder(oOrder.getId(), oRateInfo);
		if (oGetOrder.isDone() || oGetOrder.isCanceled() || oGetOrder.isError() || oGetOrder.isException() || oGetOrder.isNull())
			return;
		
		if (oGetOrder.getVolume().compareTo(nMinTradeVolume) > 0)
			return;
		
		final BigDecimal nFreeSum = oTradeControler.getTradesInfo().getSum();
		final BigDecimal oMinTradeSum = TradeUtils.getMinTradeSum(oRateInfo); 
		if (nFreeSum.compareTo(oMinTradeSum) < 0)
			return;
		
		final Date oFithteenMinutesDateCreate = DateUtils.addMinutes(new Date(), -1); 
	    if (null == oGetOrder.getCreated() || oGetOrder.getCreated().after(oFithteenMinutesDateCreate))
	    	return;
		
		removeSellOrder(oTaskTrade, oGetOrder, "SimpleTradeStrategy.removeSellIfSmall");
	}
	
	@Override public boolean isCreateNewTrade(final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
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

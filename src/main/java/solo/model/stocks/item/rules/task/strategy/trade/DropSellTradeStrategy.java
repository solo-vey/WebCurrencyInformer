package solo.model.stocks.item.rules.task.strategy.trade;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.item.analyse.CandlestickType;
import solo.model.stocks.item.analyse.StockCandlestick;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.item.rules.task.trade.TradesInfo;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;

public class DropSellTradeStrategy extends SimpleTradeStrategy
{
	private static final long serialVersionUID = 451339411265580800L;
	
	public static final String NAME = "DropSellTrade";

	@Override public String getName()
	{
		return NAME;
	}	
	
	@Override public boolean checkTrade(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		super.checkTrade(oTaskTrade, aTaskTrades, oTradeControler);
		
		final boolean bIsRemoveTrade = removeBuyIfFall(oTaskTrade, oTradeControler);
		resetCriticalPriceForSellOrder(oTaskTrade, aTaskTrades, oTradeControler);
		
		return bIsRemoveTrade;
	}

	protected boolean removeBuyIfFall(final ITradeTask oTaskTrade, final TradeControler oTradeControler)
	{
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
		if (!OrderSide.BUY.equals(oOrder.getSide()))
			return false;
		
		final RateInfo oRateInfo = oTaskTrade.getTradeInfo().getRateInfo();
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oRateInfo);
		final CandlestickType oCandlestickType = oCandlestick.getType();
		if (!oCandlestickType.isFall())
			return false;

		final BigDecimal nFreeSum = oTradeControler.getTradesInfo().getFreeSum();
		final BigDecimal nFreeSumAndOrderSum = nFreeSum.add(oOrder.getSum());
		final BigDecimal oMinTradeSum = TradeUtils.getMinTradeSum(oRateInfo); 
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oRateInfo);
		
		if (oOrder.getVolume().compareTo(nMinTradeVolume) > 0 || nFreeSumAndOrderSum.compareTo(oMinTradeSum) > 0)
		{
			TradeUtils.removeOrder(oOrder, oTaskTrade.getTradeInfo().getRateInfo());
			return true;
		}
		
		return false;
	}

	protected void resetCriticalPriceForSellOrder(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
		if (!OrderSide.SELL.equals(oOrder.getSide()))
			return;

		final int nMaxTrades = oTradeControler.getMaxTrades();
		final TradesInfo oTradesInfo = oTradeControler.getTradesInfo();
		final boolean bIsBuyPrecent = getIsOrderSidePrecent(aTaskTrades, OrderSide.BUY);
		if (bIsBuyPrecent || aTaskTrades.size() < nMaxTrades)
			return;
		
		final RateInfo oRateInfo = oTradesInfo.getRateInfo();
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oRateInfo);
		if (oOrder.getVolume().compareTo(nMinTradeVolume) < 0)
	    	return;
		
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oRateInfo);
		final CandlestickType oCandlestickType = oCandlestick.getType();
		if (!oCandlestickType.isCalm())
			return;
	
		final Date oFithteenMinutesDateCreate = DateUtils.addMinutes(new Date(), -15); 
	    if (null != oOrder.getCreated() && oOrder.getCreated().before(oFithteenMinutesDateCreate))
	    {
	    	final BigDecimal nNewCriticalPrice = oCandlestick.getAverageMaxPrice(3);
			final BigDecimal nMinCriticalPrice = oTaskTrade.getTradeInfo().getMinCriticalPrice();
	    	if (nNewCriticalPrice.compareTo(nMinCriticalPrice) > 0)
	    	{
	    		oTaskTrade.getTradeInfo().setCriticalPrice(nNewCriticalPrice);
	    		WorkerFactory.getMainWorker().sendMessage(oRateInfo + "\r\n" + oTaskTrade.getInfo(null) + "\r\n" +
	    				"Reset after 15 minutes critical price " + MathUtils.toCurrencyString(nNewCriticalPrice));
	    		return;
	    	}
	    }
	    
		final Date oHourDateCreate = DateUtils.addMinutes(new Date(), -60); 
	    if (null != oOrder.getCreated() && oOrder.getCreated().before(oHourDateCreate))
	    {
	    	final BigDecimal nNewCriticalPrice = oCandlestick.getAverageMaxPrice(3);
			final BigDecimal nMinCriticalPrice = MathUtils.getBigDecimal(oOrder.getPrice().doubleValue() * 0.9, TradeUtils.DEFAULT_PRICE_PRECISION);
	    	if (nNewCriticalPrice.compareTo(nMinCriticalPrice) > 0)
	    	{
	    		oTaskTrade.getTradeInfo().setCriticalPrice(nNewCriticalPrice);
	    		WorkerFactory.getMainWorker().sendMessage(oRateInfo + "\r\n" + oTaskTrade.getInfo(null) + "\r\n" +
	    				"Reset after hour critical price " + MathUtils.toCurrencyString(nNewCriticalPrice));
	    		return;
	    	}
	    }
	    
	}
}

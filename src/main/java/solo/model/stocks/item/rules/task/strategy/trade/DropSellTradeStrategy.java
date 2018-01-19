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
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.item.rules.task.trade.TradesInfo;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;

public class DropSellTradeStrategy extends SimpleTradeStrategy
{
	public static final String NAME = "DropSellTrade";

	@Override public String getName()
	{
		return NAME;
	}	
	
	@Override public void checkTrade(final ITradeTask oTaskTrade, final int nMaxTrades, final List<ITradeTask> aTaskTrades, final TradesInfo oTradesInfo)
	{
		final boolean bIsBuyPrecent = getIsOrderSidePrecent(aTaskTrades, OrderSide.BUY);
		if (bIsBuyPrecent || aTaskTrades.size() < nMaxTrades)
			return;
		
		final RateInfo oRateInfo = oTradesInfo.getRateInfo();
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oRateInfo);
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
	    if (oOrder.getVolume().compareTo(nMinTradeVolume) < 0)
	    	return;
	    
		final Date oMaxDateCreate = DateUtils.addMinutes(new Date(), -15); 
	    if (null == oOrder.getCreated() || oOrder.getCreated().after(oMaxDateCreate))
	    	return;
		
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oRateInfo);
		final CandlestickType oCandlestickType = oCandlestick.getType();
		if (!oCandlestickType.isCalm())
			return;
	    
    	final BigDecimal nNewCriticalPrice = oCandlestick.getAverageMaxPrice(3);
		final BigDecimal nMinCriticalPrice = oTaskTrade.getTradeInfo().getMinCriticalPrice();
    	if (nNewCriticalPrice.compareTo(nMinCriticalPrice) > 0)
    	{
    		oTaskTrade.getTradeInfo().setCriticalPrice(nNewCriticalPrice);
    		WorkerFactory.getMainWorker().sendMessage(oRateInfo + "\r\n" + oTaskTrade.getInfo(null) + "\r\n" +
    				"Reset critical price " + MathUtils.toCurrencyString(nNewCriticalPrice));
    	}
	}
}

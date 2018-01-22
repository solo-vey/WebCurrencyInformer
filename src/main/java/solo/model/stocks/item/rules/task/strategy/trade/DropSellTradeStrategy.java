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
		
		final Order oGetOrder = WorkerFactory.getStockSource().getOrder(oOrder.getId(), oRateInfo);
		if (oGetOrder.isDone() || oGetOrder.isCanceled() || oGetOrder.isError() || oGetOrder.isException() || oGetOrder.isNull())
			return false;

		final BigDecimal nFreeSum = oTradeControler.getTradesInfo().getFreeSum();
		final BigDecimal nFreeSumAndOrderSum = nFreeSum.add(oGetOrder.getSum());
		final BigDecimal oMinTradeSum = TradeUtils.getMinTradeSum(oRateInfo); 
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oRateInfo);
		
		if (oGetOrder.getVolume().compareTo(nMinTradeVolume) < 0 && nFreeSumAndOrderSum.compareTo(oMinTradeSum) < 0)
			return false;
		
		final Order oRemoveOrder = TradeUtils.removeOrder(oGetOrder, oTaskTrade.getTradeInfo().getRateInfo());
		if (oRemoveOrder.isDone())
			return false;
		
		oTaskTrade.getTradeInfo().getHistory().addToHistory("DropSellTradeStrategy.removeBuyIfFall. Remove order [" + oGetOrder.getId() + "] [" + oGetOrder + "]. " + oCandlestickType);
		return true;
	}

	protected void resetCriticalPriceForSellOrder(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
		if (!OrderSide.SELL.equals(oOrder.getSide()))
			return;

		final TradesInfo oTradesInfo = oTradeControler.getTradesInfo();
		final RateInfo oRateInfo = oTradesInfo.getRateInfo();
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oRateInfo);
		if (oOrder.getVolume().compareTo(nMinTradeVolume) < 0)
	    	return;
		
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oRateInfo);
		final CandlestickType oCandlestickType = oCandlestick.getType();
		
		final Date oFithteenMinutesDateCreate = DateUtils.addMinutes(new Date(), -15); 
	    if (null != oOrder.getCreated() && oOrder.getCreated().before(oFithteenMinutesDateCreate))
	    {
	    	final BigDecimal nLostPriceAddition = MathUtils.getBigDecimal(oTaskTrade.getTradeInfo().getPriviousLossSum().doubleValue() / oTaskTrade.getTradeInfo().getBoughtVolume().doubleValue(), TradeUtils.getPricePrecision(oRateInfo));
	    	final BigDecimal nTotalBougthPrice = oTaskTrade.getTradeInfo().getAveragedBoughPrice().add(nLostPriceAddition); 
	    	final BigDecimal nNewCriticalPrice = MathUtils.getBigDecimal(nTotalBougthPrice.doubleValue() * 0.85, TradeUtils.getPricePrecision(oRateInfo));
	    	final BigDecimal nRoundedCriticalPrice = TradeUtils.getRoundedCriticalPrice(oRateInfo, nNewCriticalPrice);
	    	if (nRoundedCriticalPrice.compareTo(oTaskTrade.getTradeInfo().getCriticalPrice()) == 0)
	    		return;
		
	    	final String strMessage = oRateInfo + "\r\n" + oTaskTrade.getInfo(null) + "\r\n" +
	    			"Drop after 15 minutes critical price " + MathUtils.toCurrencyString(nNewCriticalPrice) + ". Trand " + oCandlestickType;
	    	oTaskTrade.getTradeInfo().setCriticalPrice(nNewCriticalPrice, strMessage);
			WorkerFactory.getMainWorker().sendMessage(strMessage); 
			return;
	    }
		
		if (oCandlestickType.equals(CandlestickType.BLACK_AND_TWO_WHITE) || oCandlestickType.equals(CandlestickType.THREE_WHITE))
		{
			final BigDecimal nNewCriticalPrice = TradeUtils.getRoundedCriticalPrice(oRateInfo, oTaskTrade.getTradeInfo().calculateCriticalPrice());
			final BigDecimal nRoundedCriticalPrice = TradeUtils.getRoundedCriticalPrice(oRateInfo, nNewCriticalPrice);
			if (nRoundedCriticalPrice.compareTo(oTaskTrade.getTradeInfo().getCriticalPrice()) == 0)
				return;
			
			final String strMessage = oRateInfo + "\r\n" + oTaskTrade.getInfo(null) + "\r\n" +
					"Restore critical price " + MathUtils.toCurrencyString(nNewCriticalPrice) + ". Trand " + oCandlestickType;
			oTaskTrade.getTradeInfo().setCriticalPrice(nNewCriticalPrice, strMessage);
			WorkerFactory.getMainWorker().sendMessage(strMessage); 
			return;
		}

	    if (null != oOrder.getCreated() && oOrder.getCreated().after(oFithteenMinutesDateCreate))
	    {
	    	final BigDecimal nBougthPrice = oTaskTrade.getTradeInfo().getAveragedBoughPrice(); 
	    	final BigDecimal nTradeCommision = TradeUtils.getCommisionValue(nBougthPrice, BigDecimal.ZERO);
	    	final BigDecimal nTradeMargin = TradeUtils.getMarginValue(nBougthPrice);
	    	final BigDecimal nCommisionAndMargin = nTradeMargin.add(nTradeCommision);
	    	final BigDecimal nNewCriticalPrice = nBougthPrice.add(nCommisionAndMargin);
	    	final BigDecimal nRoundedCriticalPrice = TradeUtils.getRoundedCriticalPrice(oRateInfo, nNewCriticalPrice);
	    	if (nRoundedCriticalPrice.compareTo(oTaskTrade.getTradeInfo().getCriticalPrice()) == 0)
	    		return;
		
	    	final String strMessage = oRateInfo + "\r\n" + oTaskTrade.getInfo(null) + "\r\n" +
	    			"Set less 15 minutes critical price " + MathUtils.toCurrencyString(nNewCriticalPrice);
	    	oTaskTrade.getTradeInfo().setCriticalPrice(nNewCriticalPrice, strMessage);
			WorkerFactory.getMainWorker().sendMessage(strMessage); 
			return;
	    }
		
	}
}

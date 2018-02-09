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
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.item.rules.task.trade.TradesInfo;
import solo.model.stocks.worker.WorkerFactory;

public class ReverseTradeStrategy extends SimpleTradeStrategy
{
	private static final long serialVersionUID = -1600742695316694485L;
	
	public static final String NAME = "ReverseTrade";

	@Override public String getName()
	{
		return NAME;
	}	
	
	public void checkTrades(final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		super.checkTrades(aTaskTrades, oTradeControler);
		
		final TradesInfo oTradesInfo = oTradeControler.getTradesInfo();
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oTradesInfo.getRateInfo());
		final CandlestickType oCandlestickType = oCandlestick.getType();
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oTradesInfo.getRateInfo());
		BigDecimal nOrderSellVolume = BigDecimal.ZERO; 

		boolean bIsNeedReverse = false;
		if (oCandlestickType.equals(CandlestickType.WHITE_AND_TWO_BLACK) || oCandlestickType.equals(CandlestickType.THREE_BLACK))
		{
			final Date oMaxDateCreate = DateUtils.addMinutes(new Date(), -5); 
			nOrderSellVolume = getSellVolume(aTaskTrades, nOrderSellVolume, oMaxDateCreate);
			bIsNeedReverse = (nOrderSellVolume.compareTo(nMinTradeVolume) > 0);
		}
		else
		if (oCandlestickType.isFall())
		{
			final Date oMaxDateCreate = DateUtils.addMinutes(new Date(), -15); 
			nOrderSellVolume = getSellVolume(aTaskTrades, nOrderSellVolume, oMaxDateCreate);
			bIsNeedReverse = (nOrderSellVolume.compareTo(nMinTradeVolume) > 0);
		}
		
		if (!bIsNeedReverse)
			return;		

		for(final ITradeTask oTaskTrade : aTaskTrades)
		{
			final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
			TradeUtils.removeOrder(oOrder, oTradesInfo.getRateInfo());
			oTaskTrade.setTradeControler(ITradeControler.NULL);
			oTradeControler.tradeDone((TaskTrade)oTaskTrade);
			WorkerFactory.getStockExchange().getRules().removeRule(oTaskTrade);
		}
		
		oTradesInfo.addSell(BigDecimal.ZERO, nOrderSellVolume);
		
		final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oTradesInfo.getRateInfo());
		final TradesInfo oReverseTradesInfo = oTradeControler.getAllTradesInfo().get(oReverseRateInfo);
		oReverseTradesInfo.addSell(nOrderSellVolume, BigDecimal.ZERO);
		oTradeControler.setTradesInfo(oReverseTradesInfo);
		
		oTradesInfo.setCurrentState("Reverse trade !!! ");
	}
	
	public void startNewTrade(final ITradeTask oTaskTrade, final TradeControler oTradeControler)
	{
		if (oTaskTrade.getTradeInfo().getRateInfo().getIsReverse())
			oTaskTrade.getTradeInfo().setCriticalVolume(BigDecimal.ZERO);
	}
	
	BigDecimal getSellVolume(final List<ITradeTask> aTaskTrades, BigDecimal nOrderSellVolume, final Date oMaxDateCreate)
	{
		for(final ITradeTask oTaskTrade : aTaskTrades)
		{
			final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
			if (!OrderSide.SELL.equals(oOrder.getSide()))
				return BigDecimal.ZERO;
			
			if (null == oOrder.getCreated() || oOrder.getCreated().after(oMaxDateCreate))
				continue;
		
			nOrderSellVolume = nOrderSellVolume.add(oOrder.getVolume());
		}
		return nOrderSellVolume;
	}
}

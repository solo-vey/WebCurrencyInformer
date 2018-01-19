package solo.model.stocks.item.rules.task.strategy.trade;

import java.util.List;

import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.item.analyse.CandlestickType;
import solo.model.stocks.item.analyse.StockCandlestick;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradesInfo;
import solo.model.stocks.worker.WorkerFactory;

public class SimpleTradeStrategy implements ITradeStrategy
{
	private static final long serialVersionUID = 6260569733645147335L;
	
	public static final String NAME = "SimpleTrade";

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
		if (aTaskTrades.size() >= nMaxTrades)
			return false;
		
		final TradesInfo oTradesInfo = oTradeControler.getTradesInfo();
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oTradesInfo.getRateInfo());
		final CandlestickType oCandlestickType = oCandlestick.getType();
		if (!oCandlestickType.isFall())
			return !getIsOrderSidePrecent(aTaskTrades, OrderSide.BUY);
		
		oTradesInfo.setCurrentState("Wait buy. Trand - " + oCandlestickType);
		return false;
	}
	
	public void startNewTrade(final ITradeTask oTaskTrade, final TradeControler oTradeControler)
	{
	}
	
	public static boolean getIsOrderSidePrecent(final List<ITradeTask> aTaskTrades, final OrderSide oOrderSide)
	{
		boolean bIsPrecent = false;
		for(final ITradeTask oTaskTrade : aTaskTrades)
			bIsPrecent |= oTaskTrade.getTradeInfo().getTaskSide().equals(oOrderSide);
		
		return bIsPrecent;
	}

}

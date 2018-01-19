package solo.model.stocks.item.rules.task.strategy.trade;

import java.util.List;

import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.item.analyse.CandlestickType;
import solo.model.stocks.item.analyse.StockCandlestick;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradesInfo;
import solo.model.stocks.worker.WorkerFactory;

public class SimpleTradeStrategy implements ITradeStrategy
{
	public static final String NAME = "SimpleTrade";

	@Override public String getName()
	{
		return NAME;
	}	
	
	public void checkTrade(final ITradeTask oTaskTrade, final int nMaxTrades, final List<ITradeTask> aTaskTrades, final TradesInfo oTradesInfo)
	{
	}
	
	public void checkTrades(final List<ITradeTask> aTaskTrades, final TradesInfo oTradesInfo)
	{
	}
	
	public boolean isCreateNewTrade(final List<ITradeTask> aTaskTrades, final TradesInfo oTradesInfo)
	{
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oTradesInfo.getRateInfo());
		final CandlestickType oCandlestickType = oCandlestick.getType();
		if (!oCandlestickType.isFall())
			return !getIsOrderSidePrecent(aTaskTrades, OrderSide.BUY);
		
		oTradesInfo.setCurrentState("Wait buy trand - " + oCandlestickType);
		return false;
	}
	
	public static boolean getIsOrderSidePrecent(final List<ITradeTask> aTaskTrades, final OrderSide oOrderSide)
	{
		boolean bIsPrecent = false;
		for(final ITradeTask oTaskTrade : aTaskTrades)
			bIsPrecent |= oTaskTrade.getTradeInfo().getTaskSide().equals(oOrderSide);
		
		return bIsPrecent;
	}

}

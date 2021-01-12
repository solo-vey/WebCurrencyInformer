package solo.model.stocks.item.rules.task.strategy.trade;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.item.analyse.StockCandlestick;
import solo.model.stocks.item.rules.task.strategy.StrategyUtils;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.item.rules.task.trade.TradesInfo;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;

public class CalmRateTradeStrategy extends BaseTradeStrategy
{
	private static final long serialVersionUID = 6260569777745147335L;
	
	public static final String NAME = "CalmRateTrade";

	@Override public String getName()
	{
		return NAME;
	}	
	
	@Override public boolean checkTrade(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		setBuyCriticalPrice(oTaskTrade, oTradeControler);
		return false;
	}

	protected void setBuyCriticalPrice(final ITradeTask oTaskTrade, final TradeControler oTradeControler)
	{
		if (!OrderSide.BUY.equals(oTaskTrade.getTradeInfo().getTaskSide()))
			return;
		
		final RateAnalysisResult oRateAnalysisResult = WorkerFactory.getStockExchange().getLastAnalysisResult().getRateAnalysisResult(oTaskTrade.getRateInfo());
		
		final BigDecimal nAskPrice = StrategyUtils.getBestPrice(oRateAnalysisResult.getAsksOrders());
		final BigDecimal nBidPrice = StrategyUtils.getBestPrice(oRateAnalysisResult.getBidsOrders());
		final BigDecimal nCommision = TradeUtils.getCommisionValue(nBidPrice, nAskPrice);
		final BigDecimal nMargin = TradeUtils.getMarginValue(nAskPrice, oTaskTrade.getRateInfo());
		final BigDecimal nMinDelta = nCommision.add(nMargin);
		
		final BigDecimal nMiddlePrice = getMinPrice(nMinDelta, oTradeControler.getTradesInfo());  
		final BigDecimal nTradeCommision = TradeUtils.getCommisionValue(nMiddlePrice, nMiddlePrice);
		final BigDecimal nTradeMargin = TradeUtils.getMarginValue(nMiddlePrice, oTaskTrade.getRateInfo());
		final BigDecimal nTradeDelta = nTradeCommision.add(nTradeMargin);
		final BigDecimal nHalfTradeDelta = MathUtils.getBigDecimal(nTradeDelta.doubleValue() / 2, TradeUtils.getPricePrecision(oTradeControler.getTradesInfo().getRateInfo()));
    	final BigDecimal nNewCriticalPriceMin = nMiddlePrice.add(nHalfTradeDelta.negate());
		if (oTaskTrade.getTradeInfo().getCriticalPrice().compareTo(nNewCriticalPriceMin) != 0)
    		oTaskTrade.getTradeInfo().setCriticalPrice(nNewCriticalPriceMin, "CalmRateTradeStrategy.setBuyCriticalPrice");
	}

	private BigDecimal getMinPrice(final BigDecimal nMinDelta, TradesInfo oTradesInfo)
	{
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oTradesInfo.getRateInfo());
		
		for(int nStepCount = 3; nStepCount < oCandlestick.getHistory().size(); nStepCount++)
		{
			final BigDecimal nDelta = oCandlestick.getMinMaxDelta(nStepCount);
			if (nDelta.compareTo(nMinDelta) < 0)
				continue;
			
			final BigDecimal nHalfDelta = MathUtils.getBigDecimal(nDelta.doubleValue() / 2, TradeUtils.getPricePrecision(oTradesInfo.getRateInfo()));
			return oCandlestick.getMin(nStepCount).add(nHalfDelta);
		}
		
		return BigDecimal.ZERO;
	}
}

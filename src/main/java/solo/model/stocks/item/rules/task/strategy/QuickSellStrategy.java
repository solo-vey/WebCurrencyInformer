package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.item.rules.task.strategy.StrategyUtils;
import solo.model.stocks.item.rules.task.trade.TradeInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.utils.MathUtils;

public class QuickSellStrategy extends BaseStrategy implements ISellStrategy
{
	private static final long serialVersionUID = -8250348350757548857L;
	
	public final static String NAME = "QuickSell";
	public final static double MAX_PRICE_DELTA = 0.0002;
	
	public String getName()
	{
		return NAME;
	}

	public BigDecimal getSellPrice(final RateAnalysisResult oRateAnalysisResult, final TradeInfo oTradeInfo)
	{
		final BigDecimal nBestPrice = getBestPrice(oRateAnalysisResult, oTradeInfo);
		final BigDecimal nNowSellPrice = StrategyFactory.getSellStrategy(NowSellStrategy.NAME).getSellPrice(oRateAnalysisResult, oTradeInfo);
		final BigDecimal nMinSellPrice = MathUtils.getBigDecimal(nBestPrice.doubleValue() * (1.0 - MAX_PRICE_DELTA), TradeUtils.DEFAULT_PRICE_PRECISION); 
		if (nMinSellPrice.compareTo(nNowSellPrice) <= 0) 
			return nNowSellPrice;
		return oTradeInfo.trimSellPrice(nBestPrice);
	}
	
	public BigDecimal getBestPrice(final RateAnalysisResult oRateAnalysisResult, final TradeInfo oTradeInfo)
	{
		List<Order> oAsks = oRateAnalysisResult.getAsksOrders();
		List<Order> oBids = oRateAnalysisResult.getBidsOrders();
		
		final BigDecimal oMinChangePrice = TradeUtils.getMinChangePrice().negate();

		oAsks = StrategyUtils.removeGarbageOrders(oAsks, oBids.get(0).getPrice(), OrderSide.SELL); 
		oBids = StrategyUtils.removeGarbageOrders(oBids, oAsks.get(0).getPrice(), OrderSide.BUY);

		final List<Order> oMyOrders = TradeUtils.getMyOrders();
		oAsks = StrategyUtils.removeMyOrders(oAsks, oMyOrders); 
		oBids = StrategyUtils.removeMyOrders(oBids, oMyOrders);
		oBids = StrategyUtils.removeFirstTooExpenciveAsks(oAsks, oBids);
		
		oAsks = StrategyUtils.removeFakeOrders(oAsks, null, oRateAnalysisResult.getRateInfo()); 
		oBids = StrategyUtils.removeFakeOrders(oBids, null, oRateAnalysisResult.getRateInfo());
		
		if (StrategyUtils.isDeltaTooSmall(oAsks, oBids, oRateAnalysisResult.getRateInfo()))
			oAsks = StrategyUtils.removeTooExpenciveOrders(oAsks, oRateAnalysisResult.getRateInfo());
		
		BigDecimal nBestPrice = StrategyUtils.getBestPrice(oAsks).add(oMinChangePrice);
		while (!oTradeInfo.isMoreCriticalPrice(nBestPrice))
		{
			oAsks = StrategyUtils.removeTopOrders(oAsks);
			if (oAsks.size() == 0)
				break;
			nBestPrice = StrategyUtils.getBestPrice(oAsks).add(oMinChangePrice);
		}
		
		return nBestPrice;
	}
}

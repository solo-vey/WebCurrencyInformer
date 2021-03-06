package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.rules.task.trade.TradeInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.utils.MathUtils;

public class QuickBuyStrategy extends BaseStrategy implements IBuyStrategy
{
	private static final long serialVersionUID = -4917816147504424168L;
	
	public static final String NAME = "QuickBuy";
	public static final double MAX_PRICE_DELTA = 0.0002;
	
	public String getName()
	{
		return NAME;
	}

	public BigDecimal getBuyPrice(final RateAnalysisResult oRateAnalysisResult, final TradeInfo oTradeInfo)
	{
		final BigDecimal nBestPrice = getBestPrice(oRateAnalysisResult);
		final BigDecimal nNowBuyPrice = StrategyFactory.getBuyStrategy(NowBuyStrategy.NAME).getBuyPrice(oRateAnalysisResult, oTradeInfo);
		final BigDecimal nMaxBuyPrice = MathUtils.getBigDecimal(nBestPrice.doubleValue() * (1.0 + MAX_PRICE_DELTA), TradeUtils.DEFAULT_PRICE_PRECISION); 
		if (nMaxBuyPrice.compareTo(nNowBuyPrice) >= 0)
			return nNowBuyPrice;
		
		return nBestPrice;
	}
	
	public BigDecimal getBestPrice(final RateAnalysisResult oRateAnalysisResult)
	{
		List<Order> oAsks = oRateAnalysisResult.getAsksOrders(); 
		List<Order> oBids = oRateAnalysisResult.getBidsOrders();

		oAsks = StrategyUtils.removeGarbageOrders(oAsks, oBids.get(0).getPrice(), OrderSide.SELL); 
		oBids = StrategyUtils.removeGarbageOrders(oBids, oAsks.get(0).getPrice(), OrderSide.BUY);
		
		final BigDecimal oMinChangePrice = TradeUtils.getMinChangePrice();
		
		final List<Order> oMyOrders = TradeUtils.getMyOrders();
		oAsks = StrategyUtils.removeMyOrders(oAsks, oMyOrders); 
		oBids = StrategyUtils.removeMyOrders(oBids, oMyOrders);
		oBids = StrategyUtils.removeFirstTooExpenciveBids(oAsks, oBids);
		if (!StrategyUtils.isDeltaTooSmall(oAsks, oBids, oRateAnalysisResult.getRateInfo()))
			return StrategyUtils.getBestPrice(oBids).add(oMinChangePrice);

		oAsks = StrategyUtils.removeFakeOrders(oAsks, null, oRateAnalysisResult.getRateInfo()); 
		oBids = StrategyUtils.removeFakeOrders(oBids, null, oRateAnalysisResult.getRateInfo());
		if (!StrategyUtils.isDeltaTooSmall(oAsks, oBids, oRateAnalysisResult.getRateInfo()))
			return StrategyUtils.getBestPrice(oBids).add(oMinChangePrice);
		
		oAsks = StrategyUtils.removeTooExpenciveOrders(oAsks, oRateAnalysisResult.getRateInfo()); 
		oBids = StrategyUtils.removeTooExpenciveOrders(oBids, oRateAnalysisResult.getRateInfo());
		if (!StrategyUtils.isDeltaTooSmall(oAsks, oBids, oRateAnalysisResult.getRateInfo()))
			return StrategyUtils.getBestPrice(oBids).add(oMinChangePrice);

		while(true)
		{
			oAsks = StrategyUtils.removeTopOrders(oAsks); 
			oBids = StrategyUtils.removeTopOrders(oBids);
			if (!StrategyUtils.isDeltaTooSmall(oAsks, oBids, oRateAnalysisResult.getRateInfo()))
				return StrategyUtils.getBestPrice(oBids).add(oMinChangePrice);
		}
	}

}

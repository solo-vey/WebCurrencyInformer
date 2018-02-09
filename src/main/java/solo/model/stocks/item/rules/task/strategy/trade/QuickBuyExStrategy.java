package solo.model.stocks.item.rules.task.strategy.trade;

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

public class QuickBuyExStrategy extends QuickBuyStrategy
{
	private static final long serialVersionUID = -4917817147504424168L;
	
	public final static String NAME = "QuickBuyEx";
	
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
	
	public BigDecimal getBestPrice(final RateAnalysisResult oRateAnalysisResult, final TradeInfo oTradeInfo)
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
		if (!isDeltaTooSmall(oAsks, oBids, oTradeInfo))
			return StrategyUtils.getBestPrice(oBids).add(oMinChangePrice);

		oAsks = StrategyUtils.removeFakeOrders(oAsks, null, oRateAnalysisResult.getRateInfo()); 
		oBids = StrategyUtils.removeFakeOrders(oBids, null, oRateAnalysisResult.getRateInfo());
		if (!isDeltaTooSmall(oAsks, oBids, oTradeInfo))
			return StrategyUtils.getBestPrice(oBids).add(oMinChangePrice);
		
		oAsks = StrategyUtils.removeTooExpenciveOrders(oAsks); 
		oBids = StrategyUtils.removeTooExpenciveOrders(oBids);
		if (!isDeltaTooSmall(oAsks, oBids, oTradeInfo))
			return StrategyUtils.getBestPrice(oBids).add(oMinChangePrice);

		while(true)
		{
			oAsks = StrategyUtils.removeTopOrders(oAsks); 
			oBids = StrategyUtils.removeTopOrders(oBids);
			if (!isDeltaTooSmall(oAsks, oBids, oTradeInfo))
				return StrategyUtils.getBestPrice(oBids).add(oMinChangePrice);
		}
	}
	
	public static boolean isDeltaTooSmall(List<Order> oAsks, List<Order> oBids, final TradeInfo oTradeInfo)
	{
		final boolean bIsDeltaTooSmall = StrategyUtils.isDeltaTooSmall(oAsks, oBids);
		final BigDecimal nBidPrice = StrategyUtils.getBestPrice(oBids);
		final BigDecimal nVolume = MathUtils.getBigDecimal(oTradeInfo.getTradeSum().doubleValue() / nBidPrice.doubleValue(), TradeUtils.getVolumePrecision(oTradeInfo.getRateInfo()));
		return nVolume.compareTo(oTradeInfo.getCriticalVolume()) < 0 || bIsDeltaTooSmall;
	}

}

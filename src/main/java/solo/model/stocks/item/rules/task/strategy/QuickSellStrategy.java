package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.rules.task.trade.TradeUtils;

public class QuickSellStrategy extends BaseStrategy implements ISellStrategy
{
	private static final long serialVersionUID = -8250348350757548857L;
	
	public final static String NAME = "QuickSell";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getSellPrice(final RateAnalysisResult oRateAnalysisResult)
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
		
		if (StrategyUtils.isDeltaTooSmall(oAsks, oBids))
			oAsks = StrategyUtils.removeTooExpenciveOrders(oAsks);
		
		return StrategyUtils.getBestPrice(oAsks).add(oMinChangePrice);
	}
}

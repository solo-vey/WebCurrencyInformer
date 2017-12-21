package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.rules.task.trade.TradeUtils;

public class QuickBuyStrategy extends BaseStrategy implements IBuyStrategy
{
	private static final long serialVersionUID = -4917816147504424168L;
	
	public final static String NAME = "QuickBuy";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getBuyPrice(final RateAnalysisResult oRateAnalysisResult)
	{
		List<Order> oAsks = oRateAnalysisResult.getAsksOrders(); 
		List<Order> oBids = oRateAnalysisResult.getBidsOrders();

		oAsks = StrategyUtils.removeGarbageOrders(oAsks, oBids.get(0).getPrice(), OrderSide.SELL); 
		oBids = StrategyUtils.removeGarbageOrders(oBids, oAsks.get(0).getPrice(), OrderSide.BUY);
		
		final List<Order> oMyOrders = TradeUtils.getMyOrders();
		oAsks = StrategyUtils.removeMyOrders(oAsks, oMyOrders); 
		oBids = StrategyUtils.removeMyOrders(oBids, oMyOrders);
		if (!StrategyUtils.isDeltaTooSmall(oAsks, oBids))
			return StrategyUtils.getBestPrice(oBids).add(BigDecimal.ONE);

		oAsks = StrategyUtils.removeFakeOrders(oAsks, null); 
		oBids = StrategyUtils.removeFakeOrders(oBids, null);
		if (!StrategyUtils.isDeltaTooSmall(oAsks, oBids))
			return StrategyUtils.getBestPrice(oBids).add(BigDecimal.ONE);
		
		oAsks = StrategyUtils.removeTooExpenciveOrders(oAsks); 
		oBids = StrategyUtils.removeTooExpenciveOrders(oBids);
		if (!StrategyUtils.isDeltaTooSmall(oAsks, oBids))
			return StrategyUtils.getBestPrice(oBids).add(BigDecimal.ONE);

		while(true)
		{
			oAsks = StrategyUtils.removeTopOrders(oAsks); 
			oBids = StrategyUtils.removeTopOrders(oBids);
			if (!StrategyUtils.isDeltaTooSmall(oAsks, oBids))
				return StrategyUtils.getBestPrice(oBids).add(BigDecimal.ONE);
		}
	}

}

package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;

public class QuickBuyStrategy extends BaseStrategy implements IBuyStrategy
{
	private static final long serialVersionUID = -4917816147504424168L;
	
	public final static String NAME = "QuickBuy";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getBuyPrice(final RateAnalysisResult oRateAnalysisResult, final Order oOrder)
	{
		List<Order> oAsks = oRateAnalysisResult.getAsksOrders(); 
		List<Order> oBids = oRateAnalysisResult.getBidsOrders();
		
		oAsks = StrategyUtils.removeMyOrders(oAsks, Arrays.asList(oOrder)); 
		oBids = StrategyUtils.removeMyOrders(oBids, Arrays.asList(oOrder));
		if (!StrategyUtils.isDeltaTooSmall(oAsks, oBids))
			return StrategyUtils.getBestPrice(oBids).add(BigDecimal.ONE);
		
		oAsks = StrategyUtils.removeFakeOrders(oAsks, new BigDecimal(500)); 
		oBids = StrategyUtils.removeFakeOrders(oBids, new BigDecimal(500));
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

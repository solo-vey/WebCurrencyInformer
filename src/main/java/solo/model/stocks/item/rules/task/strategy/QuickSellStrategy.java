package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;

public class QuickSellStrategy extends BaseStrategy implements ISellStrategy
{
	private static final long serialVersionUID = -8250348350757548857L;
	
	public final static String NAME = "QuickSell";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getSellPrice(final RateAnalysisResult oRateAnalysisResult, final Order oOrder)
	{
		List<Order> oAsks = oRateAnalysisResult.getAsksOrders();
		oAsks = StrategyUtils.removeMyOrders(oAsks, Arrays.asList(oOrder));
		oAsks = StrategyUtils.removeFakeOrders(oAsks, new BigDecimal(500));
		oAsks = StrategyUtils.removeTooExpenciveOrders(oAsks);
		return StrategyUtils.getBestPrice(oAsks).add(BigDecimal.ONE.negate());
	}
}

package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;

public interface ISellStrategy extends IStrategy
{
	BigDecimal getSellPrice(final RateAnalysisResult oRateAnalysisResult, final List<Order> oMyOrders);
}

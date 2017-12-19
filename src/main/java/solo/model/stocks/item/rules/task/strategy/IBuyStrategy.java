package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;

public interface IBuyStrategy extends IStrategy
{
	BigDecimal getBuyPrice(final RateAnalysisResult oRateAnalysisResult, final Order oOrder);
}

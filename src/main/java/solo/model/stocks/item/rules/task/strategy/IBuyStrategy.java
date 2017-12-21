package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import solo.model.stocks.analyse.RateAnalysisResult;

public interface IBuyStrategy extends IStrategy
{
	BigDecimal getBuyPrice(final RateAnalysisResult oRateAnalysisResult);
}

package solo.model.stocks.item.rules.task.strategy.manager;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import solo.model.stocks.item.RateInfo;

public interface IManagerStrategy extends Serializable
{
	Map<BigDecimal, RateInfo> getMoreProfitabilityRates();
	Map<BigDecimal, RateInfo> getUnProfitabilityRates();
}

package solo.model.stocks.item.rules.task.trade;

import solo.model.stocks.item.RateInfo;
import solo.utils.MathUtils;

public class TestTradeInfo extends TradeInfo implements ITest
{
	private static final long serialVersionUID = -4742533653977276550L;

	public TestTradeInfo(RateInfo oRateInfo, int nRuleID)
	{
		super(oRateInfo, nRuleID);
	}
	
	@Override public String getInfo()
	{
		return "[TEST][" + getRateInfo() + "] " + MathUtils.toCurrencyStringEx3(getAveragedSoldPrice()) + " / " + MathUtils.toCurrencyStringEx3(getAveragedBoughPrice()) + 
				" / " + MathUtils.toCurrencyStringEx3(getDelta());
	}
}

package solo.model.stocks.item.rules.task.trade;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.RateInfo;

public class TestTradesInfo extends TradesInfo implements ITest
{
	private static final long serialVersionUID = 2014380345686912919L;

	public TestTradesInfo(RateInfo oRateInfo, int nRuleID)
	{
		super(oRateInfo, nRuleID);
	}
	
	@Override public String getInfo()
	{
		return StringUtils.EMPTY;
		//return "[TEST] " + super.getInfo();
	}
}

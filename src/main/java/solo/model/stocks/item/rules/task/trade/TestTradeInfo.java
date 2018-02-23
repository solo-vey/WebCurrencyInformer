package solo.model.stocks.item.rules.task.trade;

import solo.model.stocks.item.RateInfo;

public class TestTradeInfo extends TradeInfo implements ITest
{
	private static final long serialVersionUID = -4742533653977276550L;

	public TestTradeInfo(RateInfo oRateInfo, int nRuleID)
	{
		super(oRateInfo, nRuleID);
	}
	
	@Override public String getInfo()
	{
		return "[TEST] " + super.getInfo().replace("\r\n", "\r\n[" + getRateInfo() + "]");
	}
}

package solo.model.stocks.item.rules.task;

import solo.model.stocks.item.RateInfo;

public class TaskQuickBuy extends TaskQuickBase
{
	private static final long serialVersionUID = -178132223757975169L;

	public TaskQuickBuy(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine);
	}

	@Override public String getType()
	{
		return "QUICKBUY";   
	}
}


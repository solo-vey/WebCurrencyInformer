package solo.model.stocks.item.rules.task;

import solo.model.stocks.item.RateInfo;

public class TaskQuickTrade extends TaskQuickBase
{
	private static final long serialVersionUID = -178132223787975169L;

	public TaskQuickTrade(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine);
	}

	@Override public String getType()
	{
		return "QUICKTRADE";   
	}
}


package solo.model.stocks.item.rules.task;

import solo.model.stocks.item.RateInfo;

public class TaskQuickSell extends TaskQuickBase
{
	private static final long serialVersionUID = -178132223657975169L;

	public TaskQuickSell(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine);
	}

	@Override public String getType()
	{
		return "QUICKSELL";   
	}
}


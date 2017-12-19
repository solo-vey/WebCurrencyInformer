package solo.model.stocks.item.rules.task.trade;

import solo.model.stocks.item.RateInfo;

public class TaskSell extends TaskTradeBase
{
	private static final long serialVersionUID = -178132223657975169L;

	public TaskSell(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine);
	}

	@Override public String getType()
	{
		return "SELL";   
	}
}


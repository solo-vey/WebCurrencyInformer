package solo.model.stocks.item.rules.task.trade;

import solo.model.stocks.item.RateInfo;

public class TaskBuy extends TaskTradeBase
{
	private static final long serialVersionUID = -178132223757975169L;

	public TaskBuy(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine);
	}

	@Override public String getType()
	{
		return "BUY";   
	}
}


package solo.model.stocks.item.rules.task.trade;

import solo.model.stocks.item.Order;

public class BuyTaskTrade extends TaskTrade
{
	public static final String NAME = "BUYTRADE";

	private static final long serialVersionUID = -178111243757975169L;

	public BuyTaskTrade(final String strCommandLine) throws Exception
	{
		super(strCommandLine);
		starTask();
	}

	public BuyTaskTrade(final String strCommandLine, final String strTemplate) throws Exception
	{
		super(strCommandLine, strTemplate);
		starTask();
	}

	@Override public String getType()
	{
		return NAME;   
	}

	protected void buyDone(final Order oOrder)
	{
		getTradeInfo().setTaskSide(null);
		getTradeInfo().addToHistory(getTradeInfo().getInfo());
		taskDone(oOrder);
	}
}


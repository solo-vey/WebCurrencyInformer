package solo.model.stocks.item.rules.task.strategy;

import java.io.Serializable;

public class BaseStrategy implements IStrategy, Serializable
{
	private static final long serialVersionUID = -4917516147504424168L;
	
	public final static String NAME = "Base";
	
	public String getName()
	{
		return NAME;
	}
}

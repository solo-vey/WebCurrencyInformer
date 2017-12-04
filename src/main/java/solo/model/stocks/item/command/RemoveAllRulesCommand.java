package solo.model.stocks.item.command;

import java.util.Map.Entry;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;

/** Формат комманды */
public class RemoveAllRulesCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "deleteAllRules";
	
	public RemoveAllRulesCommand(final String strCommandLine)
	{
		super(strCommandLine);
	}
	
	public void execute() throws Exception
	{
		super.execute();

		final IStockExchange oStockExchange = getStockExchange();
		for(final Entry<Integer, IRule> oRuleInfo : oStockExchange.getRules().getRules().entrySet())
			oStockExchange.getRules().removeRule(oRuleInfo.getKey());
	
		final ICommand oCommand = new SendMessageCommand("All rules deleted");
		getMainWorker().addCommand(oCommand);
	}
}

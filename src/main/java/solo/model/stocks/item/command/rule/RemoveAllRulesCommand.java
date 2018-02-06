package solo.model.stocks.item.command.rule;

import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды */
public class RemoveAllRulesCommand extends BaseCommand
{
	final static public String NAME = "deleteAllRules";
	
	public RemoveAllRulesCommand(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
	}
	
	public void execute() throws Exception
	{
		super.execute();

		final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
		for(final Entry<Integer, IRule> oRuleInfo : oStockExchange.getRules().getRules().entrySet())
			oStockExchange.getRules().removeRule(oRuleInfo.getKey());
	
		WorkerFactory.getMainWorker().sendSystemMessage("All rules deleted");
	}
}

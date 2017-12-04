package solo.model.stocks.item.command;

import java.util.Map.Entry;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;

/** Формат комманды 
 */
public class CheckRulesCommand extends BaseCommand
{
	final static public String NAME = "checkRules";

	public CheckRulesCommand()
	{
		super();
	}

	public CheckRulesCommand(final String strCommandLine)
	{
		super(strCommandLine);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = getStockExchange();
		final StateAnalysisResult oStateAnalysisResult = oStockExchange.getHistory().getLastAnalysisResult();
		for(final Entry<Integer, IRule> oRuleInfo : oStockExchange.getRules().getRules().entrySet())
			oRuleInfo.getValue().check(oStateAnalysisResult, oRuleInfo.getKey());
	}
}

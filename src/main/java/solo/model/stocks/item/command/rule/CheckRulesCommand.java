package solo.model.stocks.item.command.rule;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.ISystemCommand;

/** Формат комманды 
 */
public class CheckRulesCommand extends BaseCommand implements ISystemCommand
{
	final static public String NAME = "checkRules";

	public CheckRulesCommand()
	{
		super(NAME, StringUtils.EMPTY);
	}

	public CheckRulesCommand(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = getStockExchange();
		final StateAnalysisResult oStateAnalysisResult = oStockExchange.getHistory().getLastAnalysisResult();
		final IRule[] aRules = oStockExchange.getRules().getRules().values().toArray(new IRule[]{});
		for(int nRulePos = 0; nRulePos < aRules.length; nRulePos++)
		{
			final IRule oRule = aRules[nRulePos];
			final Integer nRuleID = oStockExchange.getRules().getRuleID(oRule);
			oRule.check(oStateAnalysisResult, nRuleID);	
		}
	}
}

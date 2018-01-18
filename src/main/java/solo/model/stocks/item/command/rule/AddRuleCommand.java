package solo.model.stocks.item.command.rule;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RulesFactory;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class AddRuleCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "addRule";
	final static public String RULE_TYPE = "#type#";
	
	final protected String m_strRuleInfo;
	
	public AddRuleCommand(final String strRuleInfo)
	{
		super(strRuleInfo, RULE_TYPE);
		m_strRuleInfo = strRuleInfo;
	}
	
	public String getHelp() throws Exception
	{
		return RulesFactory.getHelp(super.getHelp(), getParameter(RULE_TYPE));
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final IRule oRule = RulesFactory.getRule(m_strRuleInfo);
		WorkerFactory.getStockExchange().getRules().addRule(oRule);
		
		WorkerFactory.getMainWorker().sendMessage("Rule " + getInfo() + " add. " + BaseCommand.getCommand(GetRulesCommand.NAME));
	}
}

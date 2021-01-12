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
	public static final String NAME = "addRule";
	public static final String RULE_TYPE = "#type#";
	
	protected final String m_strRuleInfo;
	
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
		
		WorkerFactory.getMainWorker().sendSystemMessage("Rule " + getInfo() + " add");
	}
}

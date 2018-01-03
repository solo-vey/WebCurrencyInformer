package solo.model.stocks.item.command.trade;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.base.HasParameters;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class SetTaskParameterCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "setTaskParameter";
	final static public String RULE_ID_PARAMETER = "#ruleID#";
	final static public String NAME_PARAMETER = "#name#";
	final static public String VALUE_PARAMETER = "#value#";
	
	final protected Integer m_nRuleID;  
	final protected String m_strName;  
	final protected String m_strValue;  
	
	public SetTaskParameterCommand(final String strСommandLine)
	{
		super(strСommandLine, CommonUtils.mergeParameters(RULE_ID_PARAMETER, NAME_PARAMETER, VALUE_PARAMETER));
		m_nRuleID = getParameterAsInt(RULE_ID_PARAMETER);
		m_strName = getParameter(NAME_PARAMETER);
		m_strValue = getParameter(VALUE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final Rules oStockRules = getStockExchange().getRules();
		final IRule oRule = oStockRules.getRules().get(m_nRuleID);
		
		if (null == oRule)
		{
			sendMessage("Rule [" + m_nRuleID + "] is absent");
			return;
		}

		final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRule);
		if (null != oTradeTask && oTradeTask instanceof HasParameters)
		{
			((HasParameters)oTradeTask).setParameter(m_strName, m_strValue);
			sendMessage("[" + m_strName + "] = [" + m_strValue + "]\r\n" + 
					CommandFactory.makeCommandLine(GetTradeInfoCommand.class, RemoveRuleCommand.ID_PARAMETER, m_nRuleID));
		}
		else
			sendMessage(oRule.getInfo(m_nRuleID));
	}
}

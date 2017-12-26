package solo.model.stocks.item.command.trade;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.item.rules.task.TaskFactory;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class GetTradeInfoCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "getTradeInfo";
	final static public String RULE_ID_PARAMETER = "#ruleID#";
	final static public String FULL_PARAMETER = "#isFull#";
	
	final protected Integer m_nRuleID;  
	final protected Boolean m_bIsFull;  
	
	public GetTradeInfoCommand(final String strСommandLine)
	{
		super(strСommandLine, CommonUtils.mergeParameters(RULE_ID_PARAMETER, FULL_PARAMETER));
		m_nRuleID = getParameterAsInt(RULE_ID_PARAMETER);
		m_bIsFull = getParameterAsBoolean(FULL_PARAMETER);
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
		
		if (oRule instanceof TaskFactory)
		{
			final TaskBase oTask = ((TaskFactory)oRule).getTaskBase();
			if (oTask instanceof TaskTrade)
			{
				final TaskTrade oTaskTrade = (TaskTrade)oTask;
				sendMessage(oTaskTrade.getTradeInfo().getInfo());
				if (m_bIsFull)
					sendMessage(oTaskTrade.getTradeInfo().toString());
			}
			else
				sendMessage(oTask.getInfo(m_nRuleID));
		}
		else
			sendMessage(oRule.getInfo(m_nRuleID));
	}
}

package solo.model.stocks.item.command.trade;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.HasParameters;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class SetTaskParameterCommand extends BaseCommand
{
	public static final String NAME = "taskParam";
	public static final String RULE_ID_PARAMETER = "#ruleID#";
	public static final String NAME_PARAMETER = "#name#";
	public static final String VALUE_PARAMETER = "#value#";
	
	protected final Integer m_nRuleID;  
	protected final String m_strName;  
	protected final String m_strValue;  
	
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
		
		final Rules oStockRules = WorkerFactory.getStockExchange().getRules();
		final IRule oRule = oStockRules.getRules().get(m_nRuleID);
		
		if (null == oRule)
		{
			WorkerFactory.getMainWorker().sendSystemMessage("Rule [" + m_nRuleID + "] is absent");
			return;
		}

		final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRule);
		final ITradeControler oTradeControler = TradeUtils.getRuleAsTradeControler(oRule);
		if (null != oTradeTask && oTradeTask instanceof HasParameters)
		{
			((HasParameters)oTradeTask).setParameter(m_strName, m_strValue);
			WorkerFactory.getMainWorker().addCommand(new GetTradeInfoCommand(m_nRuleID.toString()));
		}
		else
		if (null != oTradeControler && oTradeControler instanceof HasParameters)
		{
			((HasParameters)oTradeControler).setParameter(m_strName, m_strValue);
			WorkerFactory.getMainWorker().addCommand(new GetTradeInfoCommand(m_nRuleID.toString()));
		}
		else
			WorkerFactory.getMainWorker().sendSystemMessage(oRule.getInfo());
	}
}

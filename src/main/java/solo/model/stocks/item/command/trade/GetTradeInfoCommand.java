package solo.model.stocks.item.command.trade;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class GetTradeInfoCommand extends BaseCommand
{
	final static public String NAME = "trade";
	final static public String RULE_ID_PARAMETER = "#ruleID#";
	final static public String FULL_PARAMETER = "#isFull#";
	
	final protected Integer m_nRuleID;  
	final protected Boolean m_bIsFull;  
	
	public GetTradeInfoCommand(final String strСommandLine)
	{
		super(strСommandLine, CommonUtils.mergeParameters(RULE_ID_PARAMETER));
		m_nRuleID = getParameterAsInt(RULE_ID_PARAMETER);
		m_bIsFull = getParameterAsBoolean(FULL_PARAMETER);
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
		String strMessage = oRule.getInfo();
		
		if (null != oTradeTask)
			strMessage = oTradeTask.getTradeInfo() + "\r\n" + oTradeTask.getTradeInfo().getInfo();
		else
		if (null != oTradeControler)
			strMessage = oTradeControler.getTradesInfo() + "\r\n" + oTradeControler.getFullInfo();

		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}
}

package solo.model.stocks.item.command.trade;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.item.rules.task.strategy.trade.DropSellTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.SimpleTradeStrategy;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.telegram.TelegramTransport;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class GetTradeInfoCommand extends BaseCommand implements IHistoryCommand
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
	
	@SuppressWarnings("unchecked")
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
		{
			strMessage = oTradeTask.getTradeInfo() + "\r\n" + oTradeTask.getTradeInfo().getInfo();
			final List<List<String>> aButtons = Arrays.asList(
								Arrays.asList("Controler=trade_" + oTradeTask.getTradeControler().getTradesInfo().getRuleID(), 
										(!oTradeTask.getTradeInfo().getOrder().isNull() ? "DelOrder=/removeorder_" + oTradeTask.getTradeInfo().getOrder().getId() : StringUtils.EMPTY), 
										"DelTrade=/removerule_" + m_nRuleID));
			
			strMessage += "BUTTONS\r\n" + TelegramTransport.getButtons(aButtons);
		}
		else
		if (null != oTradeControler)
		{
			final int nMaxTrades = Integer.parseInt(oTradeControler.getParameter(TradeControler.TRADE_COUNT_PARAMETER));
			final String strStrategy = oTradeControler.getParameter(TradeControler.TRADE_STRATEGY_PARAMETER);
			final String strNewStrategy = (strStrategy.equals(SimpleTradeStrategy.NAME) ? DropSellTradeStrategy.NAME : SimpleTradeStrategy.NAME);
			final String strTradeControlerFullInfo = oTradeControler.getFullInfo();
			strMessage = oTradeControler.getTradesInfo() + "\r\n" + strTradeControlerFullInfo;
			final String strSetParam = "taskparam_" + m_nRuleID + "_";
			
			final List<List<String>> aButtons = Arrays.asList(
								Arrays.asList("Chart=chart_" + oTradeControler.getTradesInfo().getRateInfo(), 
										(nMaxTrades > 0 ? "Stop=" + strSetParam + TradeControler.TRADE_COUNT_PARAMETER + "_-1" : "Start=" + strSetParam + TradeControler.TRADE_COUNT_PARAMETER + "_1"), 
										strNewStrategy + "=" + strSetParam + TradeControler.TRADE_STRATEGY_PARAMETER + "_" + strNewStrategy, 
										"Remove=/removerule_" + m_nRuleID));
			
			strMessage += (!strMessage.contains("BUTTONS\r\n") ? "BUTTONS\r\n" : ",") + TelegramTransport.getButtons(aButtons);
		}

		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}
}

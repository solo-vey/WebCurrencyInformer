package solo.model.stocks.item.command.trade;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.AddControlerCommand;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
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
public class GetTradeInfoCommand extends BaseCommand
{
	private static final String BUTTONS = "BUTTONS\r\n";
	public static final String NAME = "trade";
	public static final String RULE_ID_PARAMETER = "#ruleID#";
	public static final String FULL_PARAMETER = "#isFull#";
	
	protected final Integer m_nRuleID;  
	protected final Boolean m_bIsFull;  
	
	public GetTradeInfoCommand(final String strСommandLine)
	{
		super(strСommandLine, CommonUtils.mergeParameters(RULE_ID_PARAMETER));
		m_nRuleID = getParameterAsInt(RULE_ID_PARAMETER);
		m_bIsFull = getParameterAsBoolean(FULL_PARAMETER);
	}
	
	@Override public void execute() throws Exception
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
								Arrays.asList(
										(oTradeTask.getTradeControler().getTradesInfo().getRuleID() > 0 ? "Controler=trade_" + oTradeTask.getTradeControler().getTradesInfo().getRuleID() : StringUtils.EMPTY), 
										(!oTradeTask.getTradeInfo().getOrder().isNull() ? "DelOrder=/removeorder_" + oTradeTask.getTradeInfo().getOrder().getId() : StringUtils.EMPTY), 
										"DelTrade=/removerule_" + m_nRuleID));
			
			strMessage += BUTTONS + TelegramTransport.getButtons(aButtons);
		}
		else
		if (null != oTradeControler)
		{
			final String strStrategy = oTradeControler.getParameter(TradeControler.TRADE_STRATEGY_PARAMETER);
			final String strNewStrategy = (strStrategy.equals(SimpleTradeStrategy.NAME) ? DropSellTradeStrategy.NAME : SimpleTradeStrategy.NAME);
			final String strTradeControlerFullInfo = oTradeControler.getFullInfo();
			strMessage = oTradeControler.getTradesInfo() + "\r\n" + strTradeControlerFullInfo;
			final String strSetParam = "taskparam_" + m_nRuleID + "_";
			
			List<List<String>> aButtons = new LinkedList<>();
			aButtons.addAll(Arrays.asList(
								Arrays.asList("Chart=chart_" + oTradeControler.getTradesInfo().getRateInfo(), 
										(oTradeControler.getControlerState().isWork() ? "Stop=" + strSetParam + TradeControler.TRADE_COUNT_PARAMETER + "_-1" : "Start=" + strSetParam + TradeControler.TRADE_COUNT_PARAMETER + "_1"), 
										strNewStrategy + "=" + strSetParam + TradeControler.TRADE_STRATEGY_PARAMETER + "_" + strNewStrategy, 
										"Remove=/removerule_" + m_nRuleID)));
			
			for(final Entry<Integer, IRule> oRuleInfo : WorkerFactory.getStockExchange().getRules().getRules().entrySet())
	    	{
				if (!oRuleInfo.getValue().getRateInfo().equals(oTradeControler.getTradesInfo().getRateInfo()))
					continue;
				
				if (oRuleInfo.getValue().equals(oTradeControler) || null == TradeUtils.getRuleAsTradeControler(oRuleInfo.getValue()))
					continue;
				
				aButtons.add(Arrays.asList(oRuleInfo.getValue().getInfo() + "=" + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, oRuleInfo.getValue().getID())));
	     	}
			
			if (ManagerUtils.isTestObject(oTradeControler))
			{
				final RateInfo oRateInfo = oTradeControler.getTradesInfo().getRateInfo();
				final BigDecimal nSum = TradeUtils.getRoundedPrice(oRateInfo, TradeUtils.getMinTradeSum(oRateInfo).multiply(BigDecimal.valueOf(2)));
				if (nSum.compareTo(BigDecimal.ZERO) > 0)
					aButtons.add(Arrays.asList("Create controler [" + nSum + "]=" + 
							CommandFactory.makeCommandLine(AddControlerCommand.class, AddControlerCommand.RATE_PARAMETER, oRateInfo,
							AddControlerCommand.SUM_PARAMETER, nSum)));
			}
			
			strMessage += (!strMessage.contains(BUTTONS) ? BUTTONS : ",") + TelegramTransport.getButtons(aButtons);
		}

		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}
}

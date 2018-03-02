package solo.model.stocks.item.command.rule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.trade.GetTradeInfoCommand;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.item.rules.task.trade.ITest;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.telegram.TelegramTransport;

/** Формат комманды 
 */
public class GetRulesCommand extends BaseCommand
{
	final static public String NAME = "rules";
	
	public GetRulesCommand(final String strCommandLine)
	{
		super(strCommandLine, "#type#");
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final String strType = getParameter("#type#").toLowerCase();
		
		final Map<String, List<IRule>> aRulesByRate = new HashMap<String, List<IRule>>();
		int nAllCount = 0;
		for(final Entry<Integer, IRule> oRuleInfo : WorkerFactory.getStockExchange().getRules().getRules().entrySet())
		{
			final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRuleInfo.getValue());
			if (null != oTradeTask && !oTradeTask.getTradeControler().equals(ITradeControler.NULL))
				continue;
			
			nAllCount++;
			final String strRate = oRuleInfo.getValue().getRateInfo().toString();
			if (strType.contains("rate:") && !strType.contains("rate:" + strRate))
				continue;
			
			if (!aRulesByRate.containsKey(strRate))
				aRulesByRate.put(strRate, new LinkedList<IRule>());
			
			aRulesByRate.get(strRate).add(oRuleInfo.getValue());
		}
		
		final boolean bIsShowIfHasReal = !strType.contains("onlytestrules");
	   	final List<List<String>> aButtons = new LinkedList<List<String>>();
		for(final Entry<String, List<IRule>> oRateRules : aRulesByRate.entrySet())
    	{
			boolean bIsHasRealRule = false;
			for(final IRule oRule : oRateRules.getValue())
				bIsHasRealRule |= !(oRule instanceof ITest); 
			if (bIsHasRealRule != bIsShowIfHasReal)
				continue;
			
			if (!strType.contains("rate:") && bIsShowIfHasReal)
			{
				String strState = StringUtils.EMPTY;
				for(final IRule oRule : oRateRules.getValue())
				{
					final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRule);
					final ITradeControler oTradeControler = TradeUtils.getRuleAsTradeControler(oRule);
					if (null != oTradeTask)
						strState += oTradeTask.getTradeInfo().getOrder().getSide() + ";";
					if (null != oTradeControler && !ManagerUtils.isTestObject(oTradeControler))
						strState += oTradeControler.getControlerState() + ";";
				}
				
				aButtons.add(Arrays.asList("[" + oRateRules.getKey() + "] [" + strState + "]=/rules_rate:" + oRateRules.getKey()));
			}
			else
			{		
				for(final IRule oRule : oRateRules.getValue())
				{
					final String strRuleInfo = oRule.getInfo();
					aButtons.add(Arrays.asList(strRuleInfo + "=" + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, oRule.getID())));
				}
			}
     	}
		
		if (bIsShowIfHasReal && aButtons.size() < nAllCount)
			aButtons.add(Arrays.asList("#### SHOW TEST RULES ####=" + CommandFactory.makeCommandLine(GetRulesCommand.class, "type", "onlytestrules")));
		
		if (!bIsShowIfHasReal && aButtons.size() < nAllCount)
			aButtons.add(Arrays.asList("#### SHOW REAL RULES ####=" + CommandFactory.makeCommandLine(GetRulesCommand.class, "type", StringUtils.EMPTY)));
			
		WorkerFactory.getMainWorker().sendSystemMessage("Rules [" + (aButtons.size() > 0 ? aButtons.size() - 1 : 0) + "]. BUTTONS\r\n" + TelegramTransport.getButtons(aButtons));
	}
}

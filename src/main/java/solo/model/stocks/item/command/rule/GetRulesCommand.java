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
		super(strCommandLine, StringUtils.EMPTY);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final Map<String, List<IRule>> aRulesByRate = new HashMap<String, List<IRule>>();
		for(final Entry<Integer, IRule> oRuleInfo : WorkerFactory.getStockExchange().getRules().getRules().entrySet())
		{
			final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRuleInfo.getValue());
			if (null != oTradeTask && !oTradeTask.getTradeControler().equals(ITradeControler.NULL))
				continue;
			
			final ITradeControler oTradeControler = TradeUtils.getRuleAsTradeControler(oRuleInfo.getValue());
			final String strRate = (null != oTradeTask ? oTradeTask.getRateInfo().toString() : 
									(null != oTradeControler ? oTradeControler.getTradesInfo().getRateInfo().toString() : StringUtils.EMPTY));
			if (!aRulesByRate.containsKey(strRate))
				aRulesByRate.put(strRate, new LinkedList<IRule>());
			
			aRulesByRate.get(strRate).add(oRuleInfo.getValue());
		}
		
	   	final List<List<String>> aButtons = new LinkedList<List<String>>();
		for(final Entry<String, List<IRule>> oRateRules : aRulesByRate.entrySet())
    	{
			for(final IRule oRule : oRateRules.getValue())
			{
				final String strRuleInfo = oRule.getInfo();
				aButtons.add(Arrays.asList(strRuleInfo + "=" + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, oRule.getID())));
			}
     	}
			
		WorkerFactory.getMainWorker().sendSystemMessage("Rules [" + aButtons.size() + "]. BUTTONS\r\n" + TelegramTransport.getButtons(aButtons));
	}
}

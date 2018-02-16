package solo.model.stocks.item.command.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;

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
		
		final Map<String, String> aRulesByRate = new HashMap<String, String>();
		for(final Entry<Integer, IRule> oRuleInfo : WorkerFactory.getStockExchange().getRules().getRules().entrySet())
		{
			final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRuleInfo.getValue());
			if (null != oTradeTask && !oTradeTask.getTradeControler().equals(ITradeControler.NULL))
				continue;
			
			final ITradeControler oTradeControler = TradeUtils.getRuleAsTradeControler(oRuleInfo.getValue());
			final String strRate = (null != oTradeTask ? oTradeTask.getRateInfo().toString() : 
									(null != oTradeControler ? oTradeControler.getTradesInfo().getRateInfo().toString() : StringUtils.EMPTY));
			if (!aRulesByRate.containsKey(strRate))
				aRulesByRate.put(strRate, StringUtils.EMPTY);
			
			final String strRateRules = aRulesByRate.get(strRate) + oRuleInfo.getValue().getInfo() + "\r\n";
			aRulesByRate.put(strRate, strRateRules);
		}
		
		String strMessage = StringUtils.EMPTY;
		for(final Entry<String, String> oRateRules : aRulesByRate.entrySet())
			strMessage += oRateRules.getValue();

		if (StringUtils.isBlank(strMessage))
			strMessage += "No rules";
			
		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}
}

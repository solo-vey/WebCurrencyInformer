package solo.model.stocks.item.command.rule;

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
	final static public String NAME = "getRules";
	
	public GetRulesCommand(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		String strMessage = StringUtils.EMPTY;
		for(final Entry<Integer, IRule> oRuleInfo : WorkerFactory.getStockExchange().getRules().getRules().entrySet())
		{
			final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRuleInfo.getValue());
			if (null != oTradeTask && !oTradeTask.getTradeControler().equals(ITradeControler.NULL))
				continue;

			strMessage += oRuleInfo.getValue().getInfo(oRuleInfo.getKey()) + "\r\n";
		}

		if (StringUtils.isNotBlank(strMessage))
			strMessage += " " + BaseCommand.getCommand(RemoveAllRulesCommand.NAME);
		else 
			strMessage += "No rules";
			
		WorkerFactory.getMainWorker().sendMessage(strMessage);
	}
}

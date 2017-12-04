package solo.model.stocks.item.command;

import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.IRule;

/** Формат комманды 
 */
public class GetRulesCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "getRules";
	
	public GetRulesCommand(final String strCommandLine)
	{
		super(strCommandLine);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		String strMessage = StringUtils.EMPTY;
		for(final Entry<Integer, IRule> oRule : getStockExchange().getRules().getRules().entrySet())
			strMessage += oRule.getValue().getInfo(oRule.getKey()) + "\r\n";

		if (StringUtils.isNotBlank(strMessage))
			strMessage += " " + BaseCommand.getCommand(RemoveAllRulesCommand.NAME);
		else 
			strMessage += "No rules";
			
		final ICommand oCommand = new SendMessageCommand(strMessage);
		getMainWorker().addCommand(oCommand);
	}
}

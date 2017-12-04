package solo.model.stocks.item.command;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

/** Формат комманды 
 */
public class HelpCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "help";

	public HelpCommand(final String strCommandLine)
	{
		super(strCommandLine);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final Map<String, Class<?>> aAllCommands = CommandFactory.getAllCommands();
		String strMessage = StringUtils.EMPTY;
		for(final String strCommand : aAllCommands.keySet())
			strMessage += "/"  + strCommand + "\r\n";

		final ICommand oCommand = new SendMessageCommand(strMessage);
		getMainWorker().addCommand(oCommand);
	}
}

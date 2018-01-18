package solo.model.stocks.item.command.system;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.base.CommandGroup;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class HelpCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "help";
	final static public String COMMAND_PARAMETER = "#command#";
	
	final protected String m_strHelpCommand;

	public HelpCommand(final String strCommandLine)
	{
		super(strCommandLine, CommonUtils.mergeParameters(COMMAND_PARAMETER, TAIL_PARAMETER));
		m_strHelpCommand = getParameter(COMMAND_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		String strMessage = StringUtils.EMPTY;
		if (StringUtils.isBlank(m_strHelpCommand))
		{
			final Map<String, Class<?>> aAllCommands = CommandFactory.getAllCommands();
			final Map<CommandGroup, List<String>> aCommandsInGroup = CommandFactory.getAllCommandsGroup();
			for(final CommandGroup oCommandGroup : CommandGroup.values())
			{
				final List<String> aCommandNames = aCommandsInGroup.get(oCommandGroup);
				if (null == aCommandNames)
					continue;

				strMessage += oCommandGroup.toString().toUpperCase() + "\r\n";
				for(final String strCommandName : aCommandNames)
				{
					final Class<?> oCommandClass = aAllCommands.get(strCommandName);
					if (ISystemCommand.class.isAssignableFrom(oCommandClass))
						continue;
					
					strMessage += "/"  + strCommandName + " [/help_"  + strCommandName + "]\r\n";
				}
			}
		}
		else
			strMessage = CommandFactory.getCommand(m_strCommandInfo).getHelp();

		WorkerFactory.getMainWorker().sendMessage(strMessage);
	}
}

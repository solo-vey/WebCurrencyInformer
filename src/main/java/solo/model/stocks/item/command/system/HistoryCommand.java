package solo.model.stocks.item.command.system;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class HistoryCommand extends BaseCommand
{
	public static final String NAME = "history";
	
	protected final String m_strFind;

	public HistoryCommand(final String strCommandLine)
	{
		super(strCommandLine, TAIL_PARAMETER);
		m_strFind = getParameter(TAIL_PARAMETER).toLowerCase();
	}
	
	public void execute() throws Exception
	{
		super.execute();
		String strMessage = StringUtils.EMPTY;
		for(final String strCommand : WorkerFactory.getMainWorker().getHistory().getCommands())
		{
			if (StringUtils.isBlank(m_strFind) || strCommand.toLowerCase().contains(m_strFind))
				strMessage += "/"  + strCommand + "\r\n";
		}
		strMessage = (StringUtils.isNotBlank(strMessage) ? strMessage : "History is empty");

		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}
}

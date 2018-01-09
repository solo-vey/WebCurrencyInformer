package solo.model.stocks.item.command.system;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.ICommand;

/** Формат комманды 
 */
public class LastErrorsCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "lastErrors";
	
	final protected String m_strFind;

	public LastErrorsCommand(final String strCommandLine)
	{
		super(strCommandLine, TAIL_PARAMETER);
		m_strFind = getParameter(TAIL_PARAMETER).toLowerCase();
	}
	
	public void execute() throws Exception
	{
		super.execute();
		String strMessage = StringUtils.EMPTY;
		for(final String strError : getMainWorker().getLastErrors().getErrors())
		{
			if (StringUtils.isBlank(m_strFind) || strError.toLowerCase().contains(m_strFind))
				strMessage += strError + "\r\n------------\r\n";
		}
		strMessage = (StringUtils.isNotBlank(strMessage) ? strMessage : "No errors");

		final ICommand oCommand = new SendMessageCommand(strMessage);
		getMainWorker().addCommand(oCommand);
	}
}

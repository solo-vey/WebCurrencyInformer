package solo.model.stocks.item.command.system;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

public class SendMessageCommand extends BaseCommand implements ISystemCommand
{
	public static final String NAME = "sendMessage";

	protected final String m_strMessage;
	
	public SendMessageCommand(final String strMessage)
	{
		super(strMessage, StringUtils.EMPTY);
		m_strMessage = strMessage;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		WorkerFactory.getTransport().sendMessage(m_strMessage);
	}
}

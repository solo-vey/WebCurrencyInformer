package solo.model.stocks.item.command.system;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;

public class SendMessageCommand extends BaseCommand implements ISystemCommand
{
	final static public String NAME = "sendMessage";

	final protected String m_strMessage;
	
	public SendMessageCommand(final String strMessage)
	{
		super(strMessage, StringUtils.EMPTY);
		m_strMessage = strMessage;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		getTransport().sendMessage(m_strMessage);
	}
}

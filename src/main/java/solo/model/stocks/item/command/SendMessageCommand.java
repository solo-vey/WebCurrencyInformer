package solo.model.stocks.item.command;

public class SendMessageCommand extends BaseCommand
{
	final static public String NAME = "sendMessage";

	final protected String m_strMessage;
	
	public SendMessageCommand(final String strMessage)
	{
		super(strMessage);
		m_strMessage = strMessage;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		getTransport().sendMessage(m_strMessage);
	}
}

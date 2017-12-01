package solo.model.stocks.item.command;

import solo.transport.ITransport;
import solo.transport.TransportFactory;
import solo.utils.CommonUtils;

public class SendMessageCommand extends BaseCommand
{
	final static public String NAME = "sendMessage";

	final protected ITransport m_oTransport;
	final protected String m_strMessage;
	
	public SendMessageCommand(final String strMessageInfo)
	{
		this(TransportFactory.getTransport(CommonUtils.splitFirst(strMessageInfo)), CommonUtils.splitTail(strMessageInfo));
	}
	public SendMessageCommand(final ITransport oTransport, final String strMessage)
	{
		super(oTransport.getName() + " " + strMessage);
		m_oTransport = oTransport;
		m_strMessage = strMessage;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		m_oTransport.sendMessage(m_strMessage);
	}
}

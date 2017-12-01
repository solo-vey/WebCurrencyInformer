package solo.model.stocks.item.command;

import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;
import solo.transport.ITransport;
import solo.transport.ITransportMessage;
import solo.transport.ITransportMessages;
import solo.transport.TransportFactory;

/** Формат комманды 
 */
public class GetTransportMessagesCommand extends BaseCommand
{
	final static public String NAME = "getMessages";

	final protected ITransport m_oTransport;
	
	public GetTransportMessagesCommand(final String oTransportName)
	{
		this(TransportFactory.getTransport(oTransportName));
	}

	public GetTransportMessagesCommand(final ITransport oTransport)
	{
		super(oTransport.getName());
		m_oTransport = oTransport;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final ITransportMessages oMessages = m_oTransport.getMessages();
		if (null == oMessages)
			return;
		
		for(final ITransportMessage oMessage : oMessages.getMessages())
		{
			final String strCommandLine = oMessage.getText();
			final ICommand oCommand = CommandFactory.getCommand(strCommandLine);
			WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oCommand);
		}
	}
}

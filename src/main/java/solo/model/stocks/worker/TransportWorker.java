package solo.model.stocks.worker;

import solo.model.stocks.item.command.GetTransportMessagesCommand;
import solo.model.stocks.item.command.ICommand;
import solo.transport.ITransport;
import ua.lz.ep.utils.ResourceUtils;

public class TransportWorker extends BaseWorker
{
	final ITransport m_oTransport;
	
	public TransportWorker(final ITransport oTransport)
	{
		super(ResourceUtils.getIntFromResource("check.transport.timeout", oTransport.getProperties(), 4000));
		m_oTransport = oTransport;
	}
	
	@Override protected void doWork() throws Exception
	{
		super.doWork();
		final ICommand oGetTransportMessagesCommand = new GetTransportMessagesCommand(m_oTransport); 
		WorkerFactory.getWorker(WorkerType.TRANSPORT).addCommand(oGetTransportMessagesCommand);
	}
}

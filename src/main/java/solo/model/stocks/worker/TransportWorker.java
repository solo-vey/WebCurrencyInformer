package solo.model.stocks.worker;

import solo.model.stocks.item.command.GetTransportMessagesCommand;
import solo.model.stocks.item.command.ICommand;
import solo.transport.ITransport;
import ua.lz.ep.utils.ResourceUtils;

public class TransportWorker extends BaseWorker
{
	final ITransport m_oTransport;
	final MainWorker m_oMainWorker; 
	
	public TransportWorker(final ITransport oTransport, final MainWorker oMainWorker)
	{
		super(ResourceUtils.getIntFromResource("check.transport.timeout", oTransport.getProperties(), 4000), oMainWorker.getStock());
		m_oTransport = oTransport;
		m_oMainWorker = oMainWorker;
	}

	public void startWorker()
	{
		WorkerFactory.registerMainWorkerThread(getId(), m_oMainWorker);
		super.startWorker();
	}
	
	@Override protected void doWork() throws Exception
	{
		super.doWork();
		final ICommand oGetTransportMessagesCommand = new GetTransportMessagesCommand(); 
		addCommand(oGetTransportMessagesCommand);
	}
	
	public ITransport getTransport()
	{
		return m_oTransport;
	}
}
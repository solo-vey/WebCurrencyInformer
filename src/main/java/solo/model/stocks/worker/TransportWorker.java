package solo.model.stocks.worker;

import solo.model.stocks.item.command.system.GetTransportMessagesCommand;
import solo.model.stocks.item.command.base.ICommand;
import solo.transport.ITransport;
import solo.utils.ResourceUtils;

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
		super.startWorker();
		WorkerFactory.registerMainWorkerThread(getId(), m_oMainWorker);
		Thread.currentThread().setName(m_oMainWorker.getStockExchange().getStockName() + " Transport");
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

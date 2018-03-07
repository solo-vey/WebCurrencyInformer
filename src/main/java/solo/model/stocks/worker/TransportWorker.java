package solo.model.stocks.worker;

import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.command.system.GetTransportMessagesCommand;
import solo.model.stocks.item.command.base.ICommand;
import solo.transport.ITransport;
import solo.utils.ResourceUtils;

public class TransportWorker extends BaseWorker
{
	final ITransport m_oTransport;
	
	public TransportWorker(final ITransport oTransport, final IWorker oMainWorker)
	{
		super(ResourceUtils.getIntFromResource("check.transport.timeout", oTransport.getProperties(), 4000), Stocks.Uknown);
		m_oTransport = oTransport;
	}

	public void startWorker()
	{
		super.startWorker();
		Thread.currentThread().setName("Transport");
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

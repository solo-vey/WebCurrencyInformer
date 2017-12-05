package solo.model.stocks.worker;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.transport.ITransport;
import solo.transport.TransportFactory;

public class MainWorker extends BaseWorker implements IMainWorker
{
	final protected StockWorker m_oStockWorker; 
	final protected TransportWorker m_oTransportWorker; 

	public MainWorker(final Stocks oStock)
	{
		super(100, oStock);
		m_oStockWorker = new StockWorker(StockExchangeFactory.getStockExchange(m_oStock), this);
		m_oTransportWorker = new TransportWorker(TransportFactory.getTransport(m_oStock), this);
	}
	
	public void startWorker()
	{
		WorkerFactory.registerMainWorkerThread(getId(), this);

		m_oStockWorker.startWorker();
		m_oTransportWorker.startWorker();
		super.startWorker();
	}
	
	public ITransport getTransport()
	{
		return m_oTransportWorker.getTransport();
	}
	
	public IStockExchange getStockExchange()
	{
		return m_oStockWorker.getStockExchange();
	}
	
	protected void onException(final Exception e)
	{
		super.onException(e);
		
		try
		{
			m_oTransportWorker.getTransport().sendMessage("Exception : " + e.getMessage());
		}
		catch (Exception eSend)
		{
			System.err.printf("Send message exception : " + eSend + "\r\n");
		}
	}
}

package solo.model.stocks.worker;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.command.base.CommandHistory;
import solo.model.stocks.item.command.base.ICommand;
import solo.transport.ITransport;
import solo.transport.MessageLevel;
import solo.transport.TransportFactory;
import solo.utils.CommonUtils;

public class MainWorker extends BaseWorker implements IMainWorker
{
	final protected StockWorker m_oStockWorker; 
	final protected TransportWorker m_oTransportWorker; 
	final protected CommandHistory m_oHistory; 

	public MainWorker(final Stocks oStock)
	{
		super(100, oStock);
		m_oStockWorker = new StockWorker(StockExchangeFactory.getStockExchange(m_oStock), this);
		m_oTransportWorker = new TransportWorker(TransportFactory.getTransport(m_oStock), this);
		m_oHistory = new CommandHistory(StockExchangeFactory.getStockExchange(m_oStock));
	}
	
	public void startWorker()
	{
		WorkerFactory.registerMainWorkerThread(getId(), this);

		m_oStockWorker.startWorker();
		m_oTransportWorker.startWorker();
		super.startWorker();
	}
	
	public void addCommand(final ICommand oCommand)
	{
		super.addCommand(oCommand);
		getHistory().addCommand(oCommand);
	}
	
	public ITransport getTransport()
	{
		return m_oTransportWorker.getTransport();
	}
	
	public IStockExchange getStockExchange()
	{
		return m_oStockWorker.getStockExchange();
	}
	
	public CommandHistory getHistory()
	{
		return m_oHistory;
	}
	
	protected void onException(final Exception e)
	{
		super.onException(e);
		
		if (getStockExchange().getMessageLevel().isLevelHigh(MessageLevel.DEBUG))
			return;
		
		try
		{
			final String strMessage = CommonUtils.getExceptionMessage(e.getCause());			
			m_oTransportWorker.getTransport().sendMessage("Exception : " + strMessage);
		}
		catch (Exception eSend)
		{
			System.err.printf("Send message exception : " + eSend + "\r\n");
		}
	}
}

package solo.model.stocks.worker;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.command.base.CommandHistory;
import solo.model.stocks.item.command.base.ICommand;
import solo.model.stocks.item.command.base.LastErrors;
import solo.model.stocks.item.command.system.SendMessageCommand;
import solo.transport.ITransport;
import solo.transport.MessageLevel;
import solo.transport.TransportFactory;

public class MainWorker extends BaseWorker implements IMainWorker
{
	final protected StockWorker m_oStockWorker; 
	final protected TransportWorker m_oTransportWorker; 
	final protected CommandHistory m_oHistory; 
	final protected LastErrors m_oLastErrors = new LastErrors();

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
	
	public LastErrors getLastErrors()
	{
		return m_oLastErrors;
	}
	
	public void sendMessage(final String strMessage)
	{
		if (StringUtils.isBlank(strMessage))
			return;
		
		final ICommand oCommand = new SendMessageCommand(strMessage);
		addCommand(oCommand);
	}
	
	public void sendMessage(final MessageLevel oLevel, final String strMessage)
	{
		if (oLevel.equals(MessageLevel.ERROR))
			getLastErrors().addError(strMessage);
			
		if (oLevel.isLevelHigh(getStockExchange().getMessageLevel()))
			sendMessage(strMessage);
	}
}

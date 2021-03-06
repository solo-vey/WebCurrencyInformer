package solo.model.stocks.worker;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.command.base.CommandHistory;
import solo.model.stocks.item.command.base.ICommand;
import solo.model.stocks.item.command.base.LastErrors;
import solo.model.stocks.item.command.system.SendMessageCommand;
import solo.transport.MessageLevel;

public class MainWorker extends BaseWorker implements IMainWorker
{
	protected final StockWorker m_oStockWorker; 
	protected final CommandHistory m_oHistory; 
	protected final LastErrors m_oLastErrors = new LastErrors();

	public MainWorker(final Stocks oStock)
	{
		super(100, oStock);
		m_oStockWorker = new StockWorker(StockExchangeFactory.getStockExchange(m_oStock), this);
		m_oHistory = new CommandHistory(StockExchangeFactory.getStockExchange(m_oStock));
	}
	
	@Override public void startWorker()
	{
		super.startWorker();
		WorkerFactory.registerMainWorkerThread(getId(), this);
		Thread.currentThread().setName(getStockExchange().getStockName() + " Main");

		m_oStockWorker.startWorker();
	}
	
	@Override public void stopWorker()
	{
		m_oStockWorker.stopWorker();
		super.stopWorker();
	}
	
	public void addCommand(final ICommand oCommand)
	{
		super.addCommand(oCommand);
		getHistory().addCommand(oCommand);
	}

	public StockWorker getStockWorker()
	{
		return m_oStockWorker;
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
	
	public void sendSystemMessage(final String strMessage)
	{
		if (StringUtils.isBlank(strMessage))
			return;
		
		final ICommand oCommand = new SendMessageCommand("SYSTEM\r\n" + strMessage);
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

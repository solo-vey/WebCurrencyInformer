package solo.model.stocks.item.command;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;

/** Формат комманды 
 */
public class SetDefaultStockCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "setDefaultStock";
	final static public String TEMPLATE = NAME + "_%s";
	
	final protected String m_strStockName;
	
	public SetDefaultStockCommand(final String strStockName)
	{
		super(strStockName);
		m_strStockName = strStockName;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oNewDefaultStockExchange = StockExchangeFactory.getStockExchange(m_strStockName);
		if (null == oNewDefaultStockExchange)
			return;
		
		StockExchangeFactory.setDefault(oNewDefaultStockExchange);
		
		final String strMessage = "New default stock " + m_strStockName + " " + BaseCommand.getCommand(GetStocksCommand.NAME);
		final ICommand oCommand = new SendMessageCommand(TransportFactory.getTransport(TelegramTransport.NAME), strMessage);
		WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oCommand);
	}
}

package solo.model.stocks.item.command;

import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;

/** Формат комманды 
 */
public class GetStocksCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "getStocks";
	
	public GetStocksCommand(final String strCommamdLine)
	{
		super(strCommamdLine);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		String strMessage = "Default stocks - " + StockExchangeFactory.getDefault().getStockName() + "\r\n";
		for(final String strStockName : StockExchangeFactory.getAll().keySet())
			strMessage += BaseCommand.getCommand(SetDefaultStockCommand.TEMPLATE, strStockName) + "\r\n";
			
		final ICommand oCommand = new SendMessageCommand(TransportFactory.getTransport(TelegramTransport.NAME), strMessage);
		WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oCommand);
	}
}

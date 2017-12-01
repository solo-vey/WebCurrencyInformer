package solo.model.stocks.worker;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.command.ICommand;
import solo.model.stocks.item.command.LoadRateInfoCommand;
import ua.lz.ep.utils.ResourceUtils;

public class StockWorker extends BaseWorker
{
	final IStockExchange m_oStockExchange;
	
	public StockWorker(final IStockExchange oStockExchange)
	{
		super(ResourceUtils.getIntFromResource("check.stock.timeout", oStockExchange.getStockProperties(), 4000));
		m_oStockExchange = oStockExchange;
	}
	
	@Override protected void doWork() throws Exception
	{
		super.doWork();
		final ICommand oLoadRateInfoCommand = new LoadRateInfoCommand(m_oStockExchange); 
		WorkerFactory.getWorker(WorkerType.STOCK).addCommand(oLoadRateInfoCommand);
	}
}

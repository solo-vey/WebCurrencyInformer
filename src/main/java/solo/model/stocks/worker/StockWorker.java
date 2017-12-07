package solo.model.stocks.worker;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.command.base.ICommand;
import solo.model.stocks.item.command.system.LoadRateInfoCommand;
import ua.lz.ep.utils.ResourceUtils;

public class StockWorker extends BaseWorker
{
	final protected IStockExchange m_oStockExchange;
	final MainWorker m_oMainWorker; 
	
	public StockWorker(final IStockExchange oStockExchange, final MainWorker oMainWorker)
	{
		super(ResourceUtils.getIntFromResource("check.stock.timeout", oStockExchange.getStockProperties(), 4000), oMainWorker.getStock());
		m_oStockExchange = oStockExchange;
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
		final ICommand oLoadRateInfoCommand = new LoadRateInfoCommand(); 
		addCommand(oLoadRateInfoCommand);
	}
	
	public IStockExchange getStockExchange()
	{
		return m_oStockExchange;
	}
}

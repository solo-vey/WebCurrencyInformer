package solo.model.stocks.worker;

import java.util.LinkedList;
import java.util.List;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import ua.lz.ep.utils.ResourceUtils;

public class StockWorker extends BaseWorker
{
	final protected IStockExchange m_oStockExchange;
	final MainWorker m_oMainWorker; 
	final List<StockRateWorker> m_aStockRateWorkers = new LinkedList<StockRateWorker>();
	
	public StockWorker(final IStockExchange oStockExchange, final MainWorker oMainWorker)
	{
		super(ResourceUtils.getIntFromResource("check.stock.timeout", oStockExchange.getStockProperties(), 4000), oMainWorker.getStock());
		m_oStockExchange = oStockExchange;
		m_oMainWorker = oMainWorker;
	}

	public void startWorker()
	{
		if (!m_bIsManualStopped)
			return;
		super.startWorker();
		
		WorkerFactory.registerMainWorkerThread(getId(), m_oMainWorker);
		
		for(final RateInfo oRateInfo : m_oStockExchange.getStockSource().getRates())
			startRateWorker(oRateInfo);
	}

	public void startRateWorker(final RateInfo oRateInfo)
	{
		final StockRateWorker oStockRateWorker = new StockRateWorker(m_oMainWorker, oRateInfo, m_nTimeOut);
		if (m_aStockRateWorkers.contains(oStockRateWorker))
			return;
		
		m_aStockRateWorkers.add(oStockRateWorker);
		oStockRateWorker.startWorker();
	}
	
	public void stopWorker()
	{
		if (m_bIsManualStopped)
			return;
		
		for(final StockRateWorker oStockRateWorker : m_aStockRateWorkers)
			oStockRateWorker.stopWorker();
		m_aStockRateWorkers.clear();
		
		super.stopWorker();
	}

	public void stopRateWorker(final RateInfo oRateInfo)
	{
		for(int nPos = 0; nPos < m_aStockRateWorkers.size(); nPos++)
		{
			if (!m_aStockRateWorkers.get(nPos).getRateInfo().equals(oRateInfo))
				continue;
			
			m_aStockRateWorkers.get(nPos).stopWorker();
			m_aStockRateWorkers.remove(nPos);
			return;
		}
	}
	
	@Override protected void doWork() throws Exception
	{
		Thread.currentThread().setName(m_oStockExchange.getStockName() + " StockWorker");
		super.doWork();
	}
	
	public IStockExchange getStockExchange()
	{
		return m_oStockExchange;
	}
}

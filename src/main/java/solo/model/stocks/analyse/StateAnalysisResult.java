package solo.model.stocks.analyse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import solo.model.stocks.BaseObject;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;

public class StateAnalysisResult extends BaseObject
{
	final protected Map<RateInfo, RateAnalysisResult> m_oRatesAnalysisResult = Collections.synchronizedMap(new HashMap<RateInfo, RateAnalysisResult>());
	
	public StateAnalysisResult(final StockRateStates oStockRateStates, final IStockExchange oStockExchange) throws Exception
	{
		final MainWorker oMainWorker = WorkerFactory.getMainWorker();
		final List<RateInfo> oRates = oStockExchange.getStockSource().getRates();
		final ExecutorService oThreadPool = Executors.newFixedThreadPool(oRates.size());
		for (int nThreadPos = 0; nThreadPos < oRates.size(); nThreadPos++) 
		{
			final RateInfo oRateInfo = oStockExchange.getStockSource().getRates().get(nThreadPos);
			final AnalyseRateThread oAnalyseRateThread = new AnalyseRateThread(oRateInfo, m_oRatesAnalysisResult, oStockRateStates, oMainWorker);
			oThreadPool.submit(oAnalyseRateThread);
		};
		oThreadPool.shutdown();
			
		try 
		{
			oThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} 
		catch (InterruptedException e) 
		{
			WorkerFactory.getMainWorker().onException(e);
		}
	}
	
	public Set<RateInfo> getRates()
	{
		return m_oRatesAnalysisResult.keySet();
	}
	
	public RateAnalysisResult getRateAnalysisResult(final RateInfo oRateInfo)
	{
		return m_oRatesAnalysisResult.get(oRateInfo);
	}
}

class AnalyseRateThread implements Runnable 
{
	final RateInfo m_oRateInfo;
	final Map<RateInfo, RateAnalysisResult> m_oRatesAnalysisResult;
	final StockRateStates m_oStockRateStates;
	final MainWorker m_oMainWorker;

    public AnalyseRateThread(final RateInfo oRateInfo, final Map<RateInfo, RateAnalysisResult> oRatesAnalysisResult, final StockRateStates oStockRateStates, final MainWorker oMainWorker)
    {
    	m_oRateInfo = oRateInfo;
    	m_oRatesAnalysisResult = oRatesAnalysisResult;
    	m_oStockRateStates = oStockRateStates;
    	m_oMainWorker = oMainWorker;
    }

    public void run() 
    {
		try
		{
			Thread.currentThread().setName("Analyse rate " + m_oRateInfo);
			WorkerFactory.registerMainWorkerThread(Thread.currentThread().getId(), m_oMainWorker);
			final IStockExchange oStockExchange = m_oMainWorker.getStockExchange();
			final RateAnalysisResult oRateAnalysisResult = new RateAnalysisResult(m_oStockRateStates, m_oRateInfo, oStockExchange);
			m_oRatesAnalysisResult.put(m_oRateInfo, oRateAnalysisResult);
			oStockExchange.getStockCandlestick().addRateInfo(m_oRateInfo, oRateAnalysisResult);
		}
		catch (final Exception e)
		{
			WorkerFactory.getMainWorker().onException(e);
		}
    }
}

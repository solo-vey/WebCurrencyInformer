package solo.model.stocks.item.command.system;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.history.StocksHistory;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.rule.CheckRulesCommand;
import solo.model.stocks.item.command.trade.ManageStock;
import solo.model.stocks.source.IStockSource;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class LoadRateInfoCommand extends BaseCommand implements ISystemCommand
{
	final static public String NAME = "loadRate";
	
	public LoadRateInfoCommand()
	{
		super(NAME, StringUtils.EMPTY);
	}
	
	public LoadRateInfoCommand(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = getStockExchange();
		final StockRateStates oRateStates = loadStockRates(oStockExchange);
		final StateAnalysisResult oStateAnalysisResult = oStockExchange.getAnalysis().analyse(oRateStates, oStockExchange);
		oStockExchange.getHistory().addToHistory(oStateAnalysisResult);
		StocksHistory.addHistory(oStockExchange, oRateStates);
		
		getMainWorker().addCommand(new ManageStock());
		getMainWorker().addCommand(new CheckRulesCommand());
	}
	
	public StockRateStates loadStockRates(final IStockExchange oStockExchange) throws Exception
	{
		final StockRateStates oStockRateStates = new StockRateStates();

		final MainWorker oMainWorker = WorkerFactory.getMainWorker();
		final IStockSource oStockSource = oStockExchange.getStockSource();
		final List<RateInfo> oRates = oStockSource.getRates();
		final ExecutorService oThreadPool = Executors.newFixedThreadPool(oRates.size());
		for (int nThreadPos = 0; nThreadPos < oRates.size(); nThreadPos++) 
		{
			final RateInfo oRateInfo = oStockExchange.getStockSource().getRates().get(nThreadPos);
			final LoadRateThread oLoadRateThread = new LoadRateThread(oRateInfo, oStockRateStates, oMainWorker);
			oThreadPool.submit(oLoadRateThread);
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
		
		return oStockRateStates;
	}
}

class LoadRateThread implements Runnable 
{
	final RateInfo m_oRateInfo;
	final StockRateStates m_oStockRateStates;
	final MainWorker m_oMainWorker;

    public LoadRateThread(final RateInfo oRateInfo, final StockRateStates oStockRateStates, final MainWorker oMainWorker)
    {
    	m_oRateInfo = oRateInfo;
    	m_oStockRateStates = oStockRateStates;
    	m_oMainWorker = oMainWorker;
    }

    public void run() 
    {
		try
		{
			WorkerFactory.registerMainWorkerThread(Thread.currentThread().getId(), m_oMainWorker);
			final IStockExchange oStockExchange = m_oMainWorker.getStockExchange();
			final IStockSource oStockSource = oStockExchange.getStockSource();
			final RateState oRateState = oStockSource.getRateState(m_oRateInfo);
			m_oStockRateStates.addRate(oRateState);
		}
		catch (final Exception e)
		{
			WorkerFactory.getMainWorker().onException(e);
		}
    }
}
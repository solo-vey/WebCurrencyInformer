package solo.model.stocks.worker;

import java.util.HashMap;
import java.util.Map;

import solo.model.stocks.exchange.Stocks;

public class WorkerFactory
{
	protected static IWorker s_oRootWorker = new BaseWorker();
	protected static Map<Long, MainWorker> s_oThreadToWorkers = new HashMap<Long, MainWorker>();
	
	static public MainWorker getMainWorker()
	{
		return s_oThreadToWorkers.get(Thread.currentThread().getId());
	}
	
	static public void registerMainWorkerThread(final Long nThreadID, final MainWorker oWorker)
	{
		s_oThreadToWorkers.put(nThreadID, oWorker);
	}
	
	static public void start()
	{
//		(new MainWorker(Stocks.Kuna)).startWorker();
//		(new MainWorker(Stocks.BtcTrade)).startWorker();
		(new MainWorker(Stocks.Exmo)).startWorker();
		
		s_oRootWorker.run();
	}
}

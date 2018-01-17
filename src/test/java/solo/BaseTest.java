package solo;

import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;
import org.junit.Before;

public class BaseTest 
{
	@Before public void doBefore()
	{
		WorkerFactory.registerMainWorkerThread(Thread.currentThread().getId(), new MainWorker(Stocks.Mock));
	}
}

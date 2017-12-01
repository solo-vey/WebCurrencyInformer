package solo.model.stocks.worker;

import java.util.HashMap;
import java.util.Map;

import solo.model.stocks.exchange.KunaStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;

public class WorkerFactory
{
	protected static Map<WorkerType, IWorker> s_oWorkers = new HashMap<WorkerType, IWorker>();
	
	static
	{
		registerWorker(WorkerType.MAIN, new MainWorker());
		registerWorker(WorkerType.STOCK, new StockWorker(StockExchangeFactory.getStockExchange(KunaStockExchange.NAME)));
		registerWorker(WorkerType.TRANSPORT, new TransportWorker(TransportFactory.getTransport(TelegramTransport.NAME)));
	}
	
	static protected void registerWorker(final WorkerType oWorkerType, final IWorker oWorker)
	{
		s_oWorkers.put(oWorkerType, oWorker);
	}
	
	static public IWorker getWorker(final WorkerType oWorkerType)
	{
		return s_oWorkers.get(oWorkerType);
	}
	
	static public void start()
	{
		final IWorker oMainWorker = getWorker(WorkerType.MAIN);
		
		for(final IWorker oWorker : s_oWorkers.values())
		{
			if (!oWorker.equals(oMainWorker))
				oWorker.startWorker();
		}
		
		oMainWorker.run();
	}
}

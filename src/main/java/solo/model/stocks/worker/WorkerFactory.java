package solo.model.stocks.worker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.source.IStockSource;
import solo.transport.ITransport;
import solo.transport.MessageLevel;
import solo.utils.CommonUtils;

public class WorkerFactory
{
	protected static IWorker s_oRootWorker = new BaseWorker();
	protected static Map<Long, MainWorker> s_oThreadToWorkers = new HashMap<Long, MainWorker>();

	public static MainWorker getMainWorker()
	{
		return s_oThreadToWorkers.get(Thread.currentThread().getId());
	}
	
	public static ITransport getTransport()
	{
		return getMainWorker().getTransport();
	}
	
	public static IStockExchange getStockExchange()
	{
		return getMainWorker().getStockExchange();
	}
	
	public static IStockSource getStockSource()
	{
		return getStockExchange().getStockSource();
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
		
		s_oRootWorker.startWorker();
		s_oRootWorker.run();
	}
	
	public static void onException(final String strMessage, final Exception e)
	{
		System.err.printf(Thread.currentThread().getName() + 
				(StringUtils.isNotBlank(strMessage) ? " " + strMessage : StringUtils.EMPTY) + 
				" Thread exception : " + CommonUtils.getExceptionMessage(e) + "\r\n");
		
		try
		{
			final MainWorker oMainWorker = WorkerFactory.getMainWorker();
			if (null == oMainWorker)
				return;
			
			final String strFullMessage = (StringUtils.isNotBlank(strMessage) ? " " + strMessage : StringUtils.EMPTY) + 
										"Exception : " + CommonUtils.getExceptionMessage(e.getCause());			
			if (MessageLevel.DEBUG.isLevelHigh(oMainWorker.getStockExchange().getMessageLevel()))
				oMainWorker.getTransport().sendMessage(strFullMessage);
			oMainWorker.getLastErrors().addError(strFullMessage);
		}
		catch (Exception eSend)
		{
			System.err.printf("Send message exception : " + eSend + "\r\n");
		}
		
	}
}

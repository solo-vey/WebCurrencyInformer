package solo.model.stocks.worker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.rules.task.trade.ITest;
import solo.model.stocks.source.IStockSource;
import solo.transport.ITransport;
import solo.transport.MessageLevel;
import solo.transport.telegram.TelegramTransport;
import solo.utils.CommonUtils;
import solo.utils.TraceUtils;

public class WorkerFactory
{
	protected static IWorker s_oRootWorker = new BaseWorker();
	protected static TransportWorker s_oTransportWorker = new TransportWorker(new TelegramTransport(), s_oRootWorker); 
	protected static Map<Long, MainWorker> s_oThreadToWorkers = new HashMap<>();
	protected static Map<Stocks, MainWorker> s_oAllWorkers = new EnumMap<>(Stocks.class);
	protected static String s_strCurentMainWorker = StringUtils.EMPTY;
	
	static
	{
//		registerMainWorker(new MainWorker(Stocks.Kuna));
//		registerMainWorker(new MainWorker(Stocks.BtcTrade));
		registerMainWorker(new MainWorker(Stocks.Exmo));
//		registerMainWorker(new MainWorker(Stocks.Cryptopia));
//		registerMainWorker(new MainWorker(Stocks.Poloniex));
	}
	
	WorkerFactory() 
	{
		throw new IllegalStateException("Utility class");
	}
	
	public static void registerMainWorker(final MainWorker oMainWorker)
	{
		s_oAllWorkers.put(oMainWorker.getStock(), oMainWorker);
	}
	
	public static Map<Stocks, MainWorker> getAllMainWorkers()
	{
		return s_oAllWorkers;
	}
	
	public static MainWorker getMainWorker(Stocks oStock)
	{
		return getAllMainWorkers().get(oStock);
	}

	public static MainWorker getMainWorker()
	{
		final MainWorker oMainWorker = s_oThreadToWorkers.get(Thread.currentThread().getId());
		return (null != oMainWorker ? oMainWorker : (MainWorker)getCurrentMainWorker());
	}
	
	public static ITransport getTransport()
	{
		return s_oTransportWorker.getTransport();
	}
	
	public static IStockExchange getStockExchange()
	{
		return getMainWorker().getStockExchange();
	}
	
	public static IStockSource getStockSource()
	{
		return getStockExchange().getStockSource();
	}
	
	public static IStockSource getStockTestSource()
	{
		return getStockExchange().getStockTestSource();
	}
	
	public static IStockSource getStockSource(final Object oTradeObject)
	{
		return (oTradeObject instanceof ITest ?  getStockTestSource() : getStockSource());
	}
	
	public static void registerMainWorkerThread(final Long nThreadID, final MainWorker oWorker)
	{
		s_oThreadToWorkers.put(nThreadID, oWorker);
	}
	
	public static void start() throws Exception
	{
//		getMainWorker(Stocks.Kuna).startWorker();
		getMainWorker(Stocks.Exmo).startWorker();
//		getMainWorker(Stocks.Cryptopia).startWorker();
//		getMainWorker(Stocks.Poloniex).startWorker();
		
		s_oRootWorker.startWorker();
		s_oTransportWorker.startWorker();
		s_oRootWorker.run();
	}
	
	public static void setCurrentMainWorker(final String strCurentMainWorker)
	{
		s_strCurentMainWorker = strCurentMainWorker;
	}
	
	public static Stocks getCurrentSock()
	{
		for(final Stocks oStock : Stocks.values())
		{
			if (oStock.toString().equalsIgnoreCase(s_strCurentMainWorker))
				return oStock;	
		}
		
		s_strCurentMainWorker = Stocks.Exmo.toString();
		return Stocks.Exmo;
	}
	
	public static IMainWorker getCurrentMainWorker()
	{
		return getMainWorker(getCurrentSock());	
	}
	
	public static boolean isStockActive(final Stocks oStock)
	{
		if (null == getAllMainWorkers().get(oStock))
			return false;
		
		return getAllMainWorkers().get(oStock).isWork();
	}
	
	public static void onException(final String strMessage, final Exception e)
	{
		final DateFormat oDateFormat = new SimpleDateFormat("HH:mm:ss");
		final String strDate = oDateFormat.format(new Date()); 
		
		TraceUtils.writeError(strDate + " " +Thread.currentThread().getName() + 
				(StringUtils.isNotBlank(strMessage) ? "\t" + strMessage : StringUtils.EMPTY) + 
				"\tThread exception : " + CommonUtils.getExceptionMessage(e));
		
		try
		{
			final MainWorker oMainWorker = WorkerFactory.getMainWorker();
			if (null == oMainWorker)
				return;
			
			final String strFullMessage = (StringUtils.isNotBlank(strMessage) ? " " + strMessage : StringUtils.EMPTY) + 
										(null != e ? "Exception : " + CommonUtils.getExceptionMessage(e.getCause()) : StringUtils.EMPTY);
			
			if (MessageLevel.DEBUG.isLevelHigh(oMainWorker.getStockExchange().getMessageLevel()))
				getTransport().sendMessage(strFullMessage);
			oMainWorker.getLastErrors().addError(strFullMessage);
		}
		catch (Exception eSend)
		{
			TraceUtils.writeError("Send message exception : " + eSend);
		}
	}
}

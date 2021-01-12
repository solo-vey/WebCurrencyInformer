package solo.archive.transports;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import solo.model.stocks.exchange.Stocks;
import solo.transport.ITransport;
import solo.transport.MockTransport;
import solo.transport.telegram.TelegramTransport;

public class TransportFactory
{
	static final Map<Stocks, Class<?>> s_oTransportsClassByStock = new HashMap<Stocks, Class<?>>();
	static final Map<Class<?>, ITransport> s_oTransportsByClass = new HashMap<Class<?>, ITransport>();
	
	static
	{
		registerTransport(Stocks.Mock, 		MockTransport.class);
		registerTransport(Stocks.Kuna, 		TelegramTransport.class);
		registerTransport(Stocks.BtcTrade,	TelegramTransport.class);
		registerTransport(Stocks.Exmo, 		TelegramTransport.class);
		registerTransport(Stocks.Cryptopia,	TelegramTransport.class);
		registerTransport(Stocks.Poloniex,	TelegramTransport.class);
	}
	
	static void registerTransport(final Stocks oStock, final Class<?> oClass)
	{
		s_oTransportsClassByStock.put(oStock, oClass);
	}

	public static ITransport getTransport(final Stocks oStock)
	{	
		final Class<?> oClass = (Class<?>) s_oTransportsClassByStock.get(oStock);
		if (null == oClass)
			return null;
		
		if (s_oTransportsByClass.containsKey(oClass))
			return s_oTransportsByClass.get(oClass);
		
		try
		{
			final Constructor<?> oConstructor = oClass.getConstructor();
			final ITransport oTransport = (ITransport) oConstructor.newInstance(new Object[] {});
			
			s_oTransportsByClass.put(oClass, oTransport);
			return oTransport;
		}
		catch(final Exception e) { }

		return null;
	}
}

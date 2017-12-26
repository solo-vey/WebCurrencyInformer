package solo.transport;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import solo.model.stocks.exchange.Stocks;
import solo.transport.telegram.BtcTradeTelegramTransport;
import solo.transport.telegram.ExmoTelegramTransport;
import solo.transport.telegram.KunaTelegramTransport;

public class TransportFactory
{
	final static Map<Stocks, Class<?>> s_oTransportsClassByStock = new HashMap<Stocks, Class<?>>();
	
	static
	{
		registerTransport(Stocks.Kuna, 		KunaTelegramTransport.class);
		registerTransport(Stocks.BtcTrade,	BtcTradeTelegramTransport.class);
		registerTransport(Stocks.Exmo, 		ExmoTelegramTransport.class);
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
		
		try
		{
			final Constructor<?> oConstructor = oClass.getConstructor();
			return (ITransport) oConstructor.newInstance(new Object[] {});
		}
		catch(final Exception e) {}

		return null;
	}
}

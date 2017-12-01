package solo.transport;

import java.util.HashMap;
import java.util.Map;

import solo.transport.telegram.TelegramTransport;

public class TransportFactory
{
	final static Map<String, ITransport> s_oTransports = new HashMap<String, ITransport>();
	
	static
	{
		registerTransport(new TelegramTransport());
	}
	
	static void registerTransport(final ITransport oTransport)
	{
		s_oTransports.put(oTransport.getName().toLowerCase(), oTransport);
	}

	public static ITransport getTransport(final String strName)
	{
		return s_oTransports.get(strName.toLowerCase());
	}
}

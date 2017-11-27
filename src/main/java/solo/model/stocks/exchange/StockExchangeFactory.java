package solo.model.stocks.exchange;

import java.util.HashMap;
import java.util.Map;

public class StockExchangeFactory
{
	final static Map<String, IStockExchange> s_oStockExchanges = new HashMap<String, IStockExchange>();
	
	static
	{
		registerStockExchange(new KunaStockExchange());
		registerStockExchange(new MockStockExchange());
	}
	
	static void registerStockExchange(final IStockExchange oStockExchange)
	{
		s_oStockExchanges.put(oStockExchange.getStockName(), oStockExchange);
	}

	public static IStockExchange getStockExchange(final String strName)
	{
		return s_oStockExchanges.get(strName);
	}
}

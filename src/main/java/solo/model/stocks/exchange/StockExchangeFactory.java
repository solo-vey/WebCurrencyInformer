package solo.model.stocks.exchange;

import java.util.HashMap;
import java.util.Map;

public class StockExchangeFactory
{
	final static Map<String, IStockExchange> s_oStockExchanges = new HashMap<String, IStockExchange>();
	static IStockExchange s_oDefaultStockExchange;
	
	static
	{
		registerStockExchange(new KunaStockExchange());
		registerStockExchange(new MockStockExchange());
		setDefault(getStockExchange(KunaStockExchange.NAME));
	}
	
	static void registerStockExchange(final IStockExchange oStockExchange)
	{
		s_oStockExchanges.put(oStockExchange.getStockName().toLowerCase(), oStockExchange);
	}

	public static IStockExchange getStockExchange(final String strName)
	{
		return s_oStockExchanges.get(strName.toLowerCase());
	}
	
	public static IStockExchange getDefault()
	{
		return s_oDefaultStockExchange;
	}
	
	public static void setDefault(final IStockExchange oDefaultStockExchange)
	{
		s_oDefaultStockExchange = oDefaultStockExchange;
	}
}

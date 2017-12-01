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
		registerStockExchange(new BtcTradeStockExchange());
		setDefault(getStockExchange(KunaStockExchange.NAME));
	}
	
	static void registerStockExchange(final IStockExchange oStockExchange)
	{
		s_oStockExchanges.put(oStockExchange.getStockName().toLowerCase(), oStockExchange);
	}

	public static Map<String, IStockExchange> getAll()
	{
		return s_oStockExchanges;
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

package solo.model.stocks.exchange;

import java.util.HashMap;
import java.util.Map;

public class StockExchangeFactory
{
	final static Map<Stocks, IStockExchange> s_oStockExchanges = new HashMap<Stocks, IStockExchange>();
	
	static
	{
		registerStockExchange(Stocks.Mock, 		new MockStockExchange());
		registerStockExchange(Stocks.Kuna, 		new KunaStockExchange());
		registerStockExchange(Stocks.BtcTrade,	new BtcTradeStockExchange());
		registerStockExchange(Stocks.Exmo, 		new ExmoStockExchange());
		registerStockExchange(Stocks.Cryptopia,	new СryptopiaStockExchange());
	}
	
	static void registerStockExchange(final Stocks oStock, final IStockExchange oStockExchange)
	{
		s_oStockExchanges.put(oStock, oStockExchange);
	}

	public static IStockExchange getStockExchange(final Stocks oStock)
	{
		return s_oStockExchanges.get(oStock);
	}
}

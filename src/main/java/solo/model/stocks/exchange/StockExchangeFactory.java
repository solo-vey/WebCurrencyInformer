package solo.model.stocks.exchange;

import java.util.EnumMap;
import java.util.Map;

public class StockExchangeFactory
{
	static final Map<Stocks, IStockExchange> s_oStockExchanges = new EnumMap<>(Stocks.class);
	
	StockExchangeFactory() 
	{
		throw new IllegalStateException("Utility class");
	}
	
	static
	{
//		registerStockExchange(Stocks.Mock, 		new MockStockExchange());
//		registerStockExchange(Stocks.Kuna, 		new KunaStockExchange());
//		registerStockExchange(Stocks.BtcTrade,	new BtcTradeStockExchange());
		registerStockExchange(Stocks.Exmo, 		new ExmoStockExchange());
//		registerStockExchange(Stocks.Cryptopia,	new СryptopiaStockExchange());
//		registerStockExchange(Stocks.Poloniex,	new PoloniexStockExchange());
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

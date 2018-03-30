package solo.archive.exchange;

import solo.archive.source.PoloniexStockSource;
import solo.model.stocks.exchange.BaseStockExchange;

public class PoloniexStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "PoloniexStockExchange.properties";
	final public static String NAME = "Poloniex";
	
	public PoloniexStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		m_oStockSource = new PoloniexStockSource(this);
	}
}

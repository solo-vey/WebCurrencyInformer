package solo.model.stocks.exchange;

import solo.model.stocks.source.ExmoStockSource;

public class ExmoStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "ExmoStockExchange.properties";
	final public static String NAME = "Exmo";
	
	public ExmoStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		m_oStockSource = new ExmoStockSource(this);
	}
}

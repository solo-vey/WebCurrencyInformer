package solo.model.stocks.exchange;

import solo.model.stocks.source.MockStockSource;

public class MockStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "MockStockExchange.properties";
	final public static String NAME = "Mock";
	
	public MockStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		m_oStockSource = new MockStockSource(this);
	}
}

package solo.model.stocks.exchange;

import solo.model.currency.Currency;
import solo.model.stocks.item.StockCurrencyVolume;
import solo.model.stocks.source.MockStockSource;

public class MockStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "MockStockExchange.properties";
	final public static String NAME = "Mock";
	
	public MockStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		m_oStockSource = new MockStockSource(this);
		m_oStockCurrencyVolumes.put(Currency.UAH, new StockCurrencyVolume(Currency.UAH, 10000));
		m_oStockCurrencyVolumes.put(Currency.BTC, new StockCurrencyVolume(Currency.BTC, 0.05));
	}
}

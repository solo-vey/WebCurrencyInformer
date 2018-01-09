package solo.model.stocks.exchange;

import solo.model.currency.Currency;
import solo.model.stocks.item.StockCurrencyVolume;
import solo.model.stocks.source.ExmoStockSource;

public class ExmoStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "ExmoStockExchange.properties";
	final public static String NAME = "Exmo";
	
	public ExmoStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		m_oStockSource = new ExmoStockSource(this);
		m_oStockCurrencyVolumes.put(Currency.UAH, new StockCurrencyVolume(Currency.UAH, 10000));
		m_oStockCurrencyVolumes.put(Currency.BTC, new StockCurrencyVolume(Currency.BTC, 0.05));
		m_oStockCurrencyVolumes.put(Currency.WAVES, new StockCurrencyVolume(Currency.BTC, 0.05));
	}
}

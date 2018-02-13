package solo.model.stocks.exchange;

import solo.model.currency.Currency;
import solo.model.stocks.item.StockCurrencyVolume;
import solo.model.stocks.source.CryptopiaStockSource;

public class СryptopiaStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "СryptopiaStockExchange.properties";
	final public static String NAME = "Сryptopia";
	
	public СryptopiaStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		m_oStockSource = new CryptopiaStockSource(this);
		m_oStockCurrencyVolumes.put(Currency.UAH, new StockCurrencyVolume(Currency.UAH, 10000));
		m_oStockCurrencyVolumes.put(Currency.BTC, new StockCurrencyVolume(Currency.BTC, 0.05));
		m_oStockCurrencyVolumes.put(Currency.WAVES, new StockCurrencyVolume(Currency.BTC, 0.05));
	}
}

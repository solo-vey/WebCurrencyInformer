package solo.model.stocks.exchange;

import solo.model.stocks.source.CryptopiaStockSource;
import solo.model.stocks.source.TestStockSource;

public class СryptopiaStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "СryptopiaStockExchange.properties";
	final public static String NAME = "Сryptopia";
	
	public СryptopiaStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		final CryptopiaStockSource oRealStockSource = new CryptopiaStockSource(this);
		m_oStockSource = new TestStockSource(this, oRealStockSource);
	}
}

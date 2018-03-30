package solo.archive.exchange;

import solo.archive.source.CryptopiaStockSource;
import solo.model.stocks.exchange.BaseStockExchange;

public class СryptopiaStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "СryptopiaStockExchange.properties";
	final public static String NAME = "Сryptopia";
	
	public СryptopiaStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		m_oStockSource = new CryptopiaStockSource(this);
	}
}

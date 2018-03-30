package solo.archive.exchange;

import solo.archive.source.KunaStockSource;
import solo.model.stocks.exchange.BaseStockExchange;

public class KunaStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "KunaStockExchange.properties";
	final public static String NAME = "Kuna";
	
	public KunaStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		m_oStockSource = new KunaStockSource(this);
	}
}

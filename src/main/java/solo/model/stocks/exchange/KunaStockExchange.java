package solo.model.stocks.exchange;

import solo.model.stocks.source.KunaStockSource;

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

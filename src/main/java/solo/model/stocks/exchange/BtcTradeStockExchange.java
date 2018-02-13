package solo.model.stocks.exchange;

import solo.model.stocks.source.BtcTradeStockSource;

public class BtcTradeStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "BtcTradeStockExchange.properties";
	final public static String NAME = "BtcTrade";
	
	public BtcTradeStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		m_oStockSource = new BtcTradeStockSource(this);
	}
}

package solo.model.stocks.exchange;

import solo.model.currency.Currency;
import solo.model.stocks.history.StocksHistory;
import solo.model.stocks.item.StockCurrencyVolume;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.source.BtcTradeStockSource;

public class BtcTradeStockExchange extends BaseStockExchange
{
	final public static String PROPERIES_FILE = "BtcTradeStockExchange.properties";
	final public static String NAME = "BtcTrade";
	
	public BtcTradeStockExchange()
	{
		super(NAME, PROPERIES_FILE);
		m_oStockSource = new BtcTradeStockSource(this);
		m_oStockCurrencyVolumes.put(Currency.UAH, new StockCurrencyVolume(Currency.UAH, 10000));
		m_oStockCurrencyVolumes.put(Currency.BTC, new StockCurrencyVolume(Currency.BTC, 0.05));
	}
	
	public void checkStock() throws Exception
	{
		final StockRateStates oStockRateStates = m_oStockSource.getStockRates();
    	StocksHistory.addHistory(this, oStockRateStates);
	}

}

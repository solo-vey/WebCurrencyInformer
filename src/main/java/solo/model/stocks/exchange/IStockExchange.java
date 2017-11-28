package solo.model.stocks.exchange;

import solo.model.currency.Currency;
import solo.model.stocks.item.Events;
import solo.model.stocks.item.StockCurrencyVolume;
import solo.model.stocks.source.IStockSource;

public interface IStockExchange
{
	String getStockName();
	IStockSource getStockSource();
	String getStockProperties();
	StockCurrencyVolume getStockCurrencyVolume(final Currency oCurrency);
	Events getEvents();
}

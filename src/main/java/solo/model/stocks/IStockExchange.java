package solo.model.stocks;

import solo.model.currency.Currency;

public interface IStockExchange
{
	String getStockName();
	IStockSource getStockSource();
	String getStockProperties();
	StockCurrencyVolume getStockCurrencyVolume(final Currency oCurrency);
}

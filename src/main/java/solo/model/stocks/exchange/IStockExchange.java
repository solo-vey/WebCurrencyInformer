package solo.model.stocks.exchange;

import solo.model.currency.Currency;
import solo.model.stocks.analyse.IStateAnalysis;
import solo.model.stocks.history.StockRateStatesLocalHistory;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.StockCurrencyVolume;
import solo.model.stocks.source.IStockSource;
import solo.transport.MessageLevel;

public interface IStockExchange
{
	String getStockName();
	IStockSource getStockSource();
	String getStockProperties();
	StockCurrencyVolume getStockCurrencyVolume(final Currency oCurrency);
	Rules getRules();
	StockRateStatesLocalHistory getHistory();
	IStateAnalysis getAnalysis();
	MessageLevel getMessageLevel();
	void setParameter(String strName, String strValue);
	String getParameter(String strName);
}

package solo.model.stocks.exchange;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.analyse.StockCandlestick;
import solo.model.stocks.item.rules.task.manager.IStockManager;
import solo.model.stocks.source.IStockSource;
import solo.transport.MessageLevel;

public interface IStockExchange
{
	String getStockName();
	IStockSource getStockSource();
	IStockSource getStockTestSource();
	String getStockProperties();
	StockCandlestick getStockCandlestick();
	Rules getRules();
	StateAnalysisResult getLastAnalysisResult();
	MessageLevel getMessageLevel();
	void setParameter(String strName, String strValue);
	String getParameter(String strName);
	IStockManager getManager();
}

package solo.model.stocks.source;

import java.util.List;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockRateStates;

public interface IStockSource
{
	StockRateStates getStockRates() throws Exception;
	IStockExchange getStockExchange();
	List<RateInfo> getRates();
}

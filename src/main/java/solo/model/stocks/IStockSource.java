package solo.model.stocks;

import java.util.List;

public interface IStockSource
{
	StockRateStates getStockRates() throws Exception;
	IStockExchange getStockExchange();
	List<RateInfo> getRates();
}

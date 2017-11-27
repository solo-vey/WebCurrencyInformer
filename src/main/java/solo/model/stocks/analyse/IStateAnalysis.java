package solo.model.stocks.analyse;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.StockRateStates;

public interface IStateAnalysis
{
	StateAnalysisResult analyse(final StockRateStates oStockRateStates, final IStockExchange oStockExchange) throws Exception;
}

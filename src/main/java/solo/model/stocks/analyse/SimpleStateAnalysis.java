package solo.model.stocks.analyse;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.StockRateStates;

public class SimpleStateAnalysis implements IStateAnalysis 
{
	public StateAnalysisResult analyse(final StockRateStates oStockRateStates, final IStockExchange oStockExchange) throws Exception
	{
    	return new StateAnalysisResult(oStockRateStates, oStockExchange);
	}
}

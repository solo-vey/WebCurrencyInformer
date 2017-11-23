package solo.model.stocks;

public class SimpleStateAnalysis implements IStateAnalysis 
{
	public StateAnalysisResult analyse(final StockRateStates oStockRateStates, final IStockExchange oStockExchange) throws Exception
	{
    	return new StateAnalysisResult(oStockRateStates, oStockExchange);
	}
}

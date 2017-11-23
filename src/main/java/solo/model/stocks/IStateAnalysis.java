package solo.model.stocks;

public interface IStateAnalysis
{
	StateAnalysisResult analyse(final StockRateStates oStockRateStates, final IStockExchange oStockExchange) throws Exception;
}

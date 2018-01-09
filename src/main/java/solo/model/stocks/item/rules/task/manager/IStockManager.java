package solo.model.stocks.item.rules.task.manager;

import solo.model.stocks.analyse.StateAnalysisResult;

public interface IStockManager
{
	final static IStockManager NULL = new NullStockManager();

	void manage(final StateAnalysisResult oStateAnalysisResult);
}

class NullStockManager implements IStockManager
{
	public void manage(final StateAnalysisResult oStateAnalysisResult) {}
}

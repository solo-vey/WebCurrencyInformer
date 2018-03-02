package solo.model.stocks.item.rules.task.manager;

import java.math.BigDecimal;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.rules.task.trade.TaskTrade;

public interface IStockManager
{
	final static IStockManager NULL = new NullStockManager();

	void manage(final StateAnalysisResult oStateAnalysisResult);
	
	void tradeStart(final TaskTrade oTaskTrade);
	void tradeDone(final TaskTrade oTaskTrade);
	void buyDone(final TaskTrade oTaskTrade);
	void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume); 
	void addSell(final BigDecimal nReceiveSum, final BigDecimal nSoldVolume);
	
	StockManagesInfo getInfo();

	ManagerHistory getHistory(); 
}

class NullStockManager implements IStockManager
{
	final protected StockManagesInfo m_oStockManagesInfo = new StockManagesInfo();
	
	@Override public void manage(final StateAnalysisResult oStateAnalysisResult) {}
	
	@Override public void tradeStart(final TaskTrade oTaskTrade) {}
	@Override public void tradeDone(final TaskTrade oTaskTrade) {}
	@Override public void buyDone(final TaskTrade oTaskTrade) {}
	@Override public void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume) {}
	@Override public void addSell(final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) {}
	@Override public StockManagesInfo getInfo()
	{
		return m_oStockManagesInfo;
	}
	@Override public ManagerHistory getHistory()
	{
		return new ManagerHistory(null);
	} 
}

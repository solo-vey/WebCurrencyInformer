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
}

class NullStockManager implements IStockManager
{
	final protected StockManagesInfo m_oStockManagesInfo = new StockManagesInfo();
	
	public void manage(final StateAnalysisResult oStateAnalysisResult) {}
	
	public void tradeStart(final TaskTrade oTaskTrade) {}
	public void tradeDone(final TaskTrade oTaskTrade) {}
	public void buyDone(final TaskTrade oTaskTrade) {}
	public void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume) {}
	public void addSell(final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) {}
	public StockManagesInfo getInfo()
	{
		return m_oStockManagesInfo;
	} 
}

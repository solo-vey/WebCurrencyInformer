package solo.model.stocks.item.rules.task.manager;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.rules.task.money.Money;
import solo.model.stocks.item.rules.task.trade.TaskTrade;

public interface IStockManager
{
	static final IStockManager NULL = new NullStockManager();

	void manage(final StateAnalysisResult oStateAnalysisResult);
	
	String getOperations();
	void setOperations(final String strOperations);

	void tradeStart(final TaskTrade oTaskTrade);
	void tradeDone(final TaskTrade oTaskTrade);
	void buyDone(final TaskTrade oTaskTrade);
	void addBuy(final TaskTrade oTaskTrade, final BigDecimal nSpendSum, final BigDecimal nBuyVolume); 
	void addSell(final TaskTrade oTaskTrade, final BigDecimal nReceiveSum, final BigDecimal nSoldVolume);
	
	StockManagesInfo getInfo();
	Money getMoney();
	ManagerHistory getHistory(); 
}

class NullStockManager implements IStockManager
{
	protected final StockManagesInfo m_oStockManagesInfo = new StockManagesInfo();
	
	@Override public String getOperations() { return StringUtils.EMPTY; }
	@Override public void setOperations(final String strOperations) {/***/}
	
	@Override public void manage(final StateAnalysisResult oStateAnalysisResult) {/***/}
	
	@Override public void tradeStart(final TaskTrade oTaskTrade) {/***/}
	@Override public void tradeDone(final TaskTrade oTaskTrade) {/***/}
	@Override public void buyDone(final TaskTrade oTaskTrade) {/***/}
	@Override public void addBuy(final TaskTrade oTaskTrade, final BigDecimal nSpendSum, final BigDecimal nBuyVolume) {/***/}
	@Override public void addSell(final TaskTrade oTaskTrade, final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) {/***/}
	@Override public StockManagesInfo getInfo()
	{
		return m_oStockManagesInfo;
	}
	@Override public Money getMoney()
	{
		return new Money();
	}
	@Override public ManagerHistory getHistory()
	{
		return new ManagerHistory(null);
	} 
}

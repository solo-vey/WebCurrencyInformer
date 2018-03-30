package solo.model.stocks.item.rules.task.trade;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.stocks.item.RateInfo;

public interface ITradeControler extends Serializable
{
	final static ITradeControler NULL = new NullTradeControler();
	
	String getFullInfo();
	TradesInfo getTradesInfo();
	void tradeStart(final TaskTrade oTaskTrade);
	void tradeDone(final TaskTrade oTaskTrade);
	void buyDone(final TaskTrade oTaskTrade);
	void addBuy(final TaskTrade oTaskTrade, final BigDecimal nSpendSum, final BigDecimal nBuyVolume); 
	void addSell(final TaskTrade oTaskTrade, final BigDecimal nReceiveSum, final BigDecimal nSoldVolume);
	String getParameter(final String strParameterName);
	void setParameter(final String strParameterName, final String strValue);
	ControlerState getControlerState();
	void setControlerState(final ControlerState oControlerState);
}

class NullTradeControler implements ITradeControler
{
	private static final long serialVersionUID = 1648163916052411734L;
	
	@Override public String getFullInfo() { return StringUtils.EMPTY; }
	@Override public TradesInfo getTradesInfo() { return new TradesInfo(new RateInfo(Currency.UAH, Currency.UAH), -1); };
	@Override public void tradeStart(final TaskTrade oTaskTrade) {}
	@Override public void tradeDone(final TaskTrade oTaskTrade) {}
	@Override public void buyDone(final TaskTrade oTaskTrade) {}
	@Override public void addBuy(final TaskTrade oTaskTrade, final BigDecimal nSpendSum, final BigDecimal nBuyVolume) {} 
	@Override public void addSell(final TaskTrade oTaskTrade, final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) {} 
	@Override public String getParameter(final String strParameterName) { return null; }
	@Override public void setParameter(final String strParameterName, final String strValue) {}
	@Override public ControlerState getControlerState()
	{
		return ControlerState.STOPPING;
	}
	@Override public void setControlerState(ControlerState oControlerState)
	{
	} 
}

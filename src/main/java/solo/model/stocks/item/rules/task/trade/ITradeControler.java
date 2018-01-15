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
	void tradeDone(final TaskTrade oTaskTrade);
	void buyDone(final TaskTrade oTaskTrade);
	void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume); 
	void addSell(final BigDecimal nReceiveSum, final BigDecimal nSoldVolume); 
}

class NullTradeControler implements ITradeControler
{
	private static final long serialVersionUID = 1648163916052411734L;
	
	public String getFullInfo() { return StringUtils.EMPTY; }
	public TradesInfo getTradesInfo() { return new TradesInfo(new RateInfo(Currency.UAH, Currency.UAH)); };
	public void tradeDone(final TaskTrade oTaskTrade) {}
	public void buyDone(final TaskTrade oTaskTrade) {}
	public void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume) {} 
	public void addSell(final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) {} 
}

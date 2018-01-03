package solo.model.stocks.item.rules.task.trade;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.strategy.IBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.ISellStrategy;
import solo.utils.MathUtils;

public class TradeInfo extends BaseObject implements Serializable
{
	private static final long serialVersionUID = -7601846839785166296L;
	
	final RateInfo m_oRateInfo;
	protected ITradeControler m_oTradeControler = ITradeControler.NULL;
	protected String m_strHistory = StringUtils.EMPTY;

	protected Order m_oOrder = Order.NULL;

	protected BigDecimal m_nCriticalPrice;
	protected BigDecimal m_nCriticalVolume;
	
	protected IBuyStrategy m_oBuyStrategy;
	protected ISellStrategy m_oSellStrategy;
	protected OrderSide m_oTaskSide = OrderSide.BUY; 
	
	protected BigDecimal m_nTradeSum = BigDecimal.ZERO;
	protected BigDecimal m_nSpendSum = BigDecimal.ZERO;
	protected BigDecimal m_nBoughtVolume = BigDecimal.ZERO; 
	protected BigDecimal m_nNeedBoughtVolume = BigDecimal.ZERO; 
	protected BigDecimal m_nReceivedSum = BigDecimal.ZERO;
	protected BigDecimal m_nSoldVolume = BigDecimal.ZERO; 
	
	public TradeInfo(final RateInfo oRateInfo)
	{
		m_oRateInfo = oRateInfo;
		m_oBuyStrategy = TradeUtils.getBuyStrategy(m_oRateInfo);
		m_oSellStrategy = TradeUtils.getSellStrategy(m_oRateInfo);
	}
	
	public BigDecimal getDelta()
	{
		return m_nReceivedSum.add(m_nSpendSum.negate());
	}
	
	public Order getOrder()
	{
		return m_oOrder;
	}
	
	public BigDecimal getTradeSum()
	{
		return m_nTradeSum;
	}
	
	public BigDecimal getAveragedBoughPrice()
	{
		if (getSpendSum().compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;

		if (getBoughtVolume().compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;
		
		return TradeUtils.getRoundedPrice(m_oRateInfo, new BigDecimal(getSpendSum().doubleValue() / getBoughtVolume().doubleValue()));
	}
	
	public BigDecimal getAveragedSoldPrice()
	{
		if (getReceivedSum().compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;

		if (getSoldVolume().compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;
		
		return TradeUtils.getRoundedPrice(m_oRateInfo, new BigDecimal(getReceivedSum().doubleValue() / getSoldVolume().doubleValue()));
	}
	
	public BigDecimal getNeedSpendSum()
	{
		return m_nTradeSum.add(m_nSpendSum.negate());
	}
	
	public BigDecimal getNeedSellVolume()
	{
		return m_nBoughtVolume.add(m_nSoldVolume.negate());
	}
	
	public BigDecimal getNeedBoughtVolume()
	{
		return m_nNeedBoughtVolume;
	}
	
	public BigDecimal getCriticalPrice()
	{
		return m_nCriticalPrice;
	}
	
	public BigDecimal getCriticalVolume()
	{
		return m_nCriticalVolume;
	}
	
	public String getCriticalPriceString()
	{
		return MathUtils.toCurrencyString(getCriticalPrice());
	}
	
	public String getHistory()
	{
		return m_strHistory;
	}
	
	public IBuyStrategy getBuyStrategy()
	{
		return m_oBuyStrategy;
	}
	
	public ISellStrategy getSellStrategy()
	{
		return m_oSellStrategy;
	}
	
	public OrderSide getTaskSide()
	{
		return m_oTaskSide;
	}
	
	public BigDecimal getSpendSum()
	{
		return m_nSpendSum;
	}
	
	public BigDecimal getReceivedSum()
	{
		return m_nReceivedSum;
	}
	
	public BigDecimal getBoughtVolume()
	{
		return m_nBoughtVolume;
	}
	
	public BigDecimal getSoldVolume()
	{
		return m_nSoldVolume;
	}
	
	public void setOrder(Order oOrder)
	{
		m_oOrder = oOrder;
	}
	
	public void setNeedBoughtVolume(final BigDecimal nNeedBoughtVolume)
	{
		if (m_nNeedBoughtVolume.compareTo(nNeedBoughtVolume) != 0)
			addToHistory("Set need buy volume : " + MathUtils.toCurrencyStringEx(nNeedBoughtVolume)); 
		m_nNeedBoughtVolume = nNeedBoughtVolume;
	}
	
	public void setTradeSum(BigDecimal nTradeSum)
	{
		if (m_nTradeSum.compareTo(nTradeSum) != 0)
			addToHistory("Set trade sum : " + MathUtils.toCurrencyStringEx(nTradeSum)); 
		m_nTradeSum = nTradeSum;
	}
	
	public void addBuy(BigDecimal nSpendSum, BigDecimal nBuyVolume)
	{
		if (nSpendSum.compareTo(BigDecimal.ZERO) == 0 && nBuyVolume.compareTo(BigDecimal.ZERO) == 0)
			return;
		
		m_nSpendSum = m_nSpendSum.add(nSpendSum);
		m_nBoughtVolume = m_nBoughtVolume.add(nBuyVolume);
		getTradeControler().addBuy(nSpendSum, nBuyVolume);
		addToHistory("Buy : " + MathUtils.toCurrencyString(nSpendSum) + " / " + MathUtils.toCurrencyStringEx(nBuyVolume)); 
	}
	
	public void addSell(BigDecimal nReceivedSum, BigDecimal nSellVolume)
	{
		if (nReceivedSum.compareTo(BigDecimal.ZERO) == 0 && nSellVolume.compareTo(BigDecimal.ZERO) == 0)
			return;

		m_nReceivedSum = m_nReceivedSum.add(nReceivedSum);
		m_nSoldVolume = m_nSoldVolume.add(nSellVolume);
		getTradeControler().addSell(nReceivedSum, nSellVolume);
		addToHistory("Sell: " + MathUtils.toCurrencyString(nReceivedSum) + " / " + MathUtils.toCurrencyStringEx(nSellVolume)); 
	}
	
	public void setCriticalPrice(BigDecimal nCriticalPrice)
	{
		m_nCriticalPrice = TradeUtils.getRoundedCriticalPrice(m_oRateInfo, nCriticalPrice);
		addToHistory("Set critical price : " + MathUtils.toCurrencyString(nCriticalPrice)); 
	}
	
	public void setCriticalVolume(BigDecimal nCriticalVolume)
	{
		m_nCriticalVolume = nCriticalVolume;
		addToHistory("Set critical volume : " + MathUtils.toCurrencyStringEx(nCriticalVolume)); 
	}
	
	public void setHistory(String strHistory)
	{
		m_strHistory = strHistory;
	}
	
	public void setBuyStrategy(IBuyStrategy oBuyStrategy)
	{
		m_oBuyStrategy = oBuyStrategy;
		addToHistory("Set buy strategy : " + oBuyStrategy); 
	}
	
	public void setSellStrategy(ISellStrategy oSellStrategy)
	{
		m_oSellStrategy = oSellStrategy;
		addToHistory("Set sell strategy : " + oSellStrategy); 
	}
	
	public void setTaskSide(OrderSide oTaskSide)
	{
		m_oTaskSide = oTaskSide;
		addToHistory("Set task side : " + oTaskSide); 
	}
	
	protected void addToHistory(final String strMessage)
	{
		
		m_strHistory += strMessage + "\r\n";
	}
	
	protected void clearHistory()
	{
		m_strHistory = StringUtils.EMPTY;
	}

	public BigDecimal trimSellPrice(final BigDecimal oSellPrice)
	{
		return (oSellPrice.compareTo(getCriticalPrice()) < 0 ? getCriticalPrice() : oSellPrice);
	}

	public void setTradeControler(final ITradeControler oTradeControler)
	{
		m_oTradeControler = oTradeControler;
	}
	
	public ITradeControler getTradeControler()
	{
		return m_oTradeControler;
	}
	
	public String getInfo()
	{
		return "Trade: " + MathUtils.toCurrencyString(getReceivedSum()) + "-" + MathUtils.toCurrencyString(getSpendSum()) + "=" + MathUtils.toCurrencyString(getDelta()) + "\r\n " + 
				"Buy: " + MathUtils.toCurrencyString(getAveragedBoughPrice()) + "/" + MathUtils.toCurrencyStringEx(getBoughtVolume()) + "\r\n " +
				"Sell: " + MathUtils.toCurrencyString(getAveragedSoldPrice()) + "/" + MathUtils.toCurrencyStringEx(getSoldVolume());
	}
}

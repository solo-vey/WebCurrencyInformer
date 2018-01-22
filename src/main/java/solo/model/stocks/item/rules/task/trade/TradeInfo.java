package solo.model.stocks.item.rules.task.trade;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
	protected TradeHistory m_oHistory;
	protected Integer m_nRuleID;
	protected Date m_oDateCreate = new Date();

	protected Order m_oOrder = Order.NULL;

	protected BigDecimal m_nCriticalPrice;
	protected BigDecimal m_nCriticalVolume;
	protected BigDecimal m_nPriviousLossSum = BigDecimal.ZERO;
	
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
		addToHistory("Create trade");
	}
	
	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}
	
	public BigDecimal getDelta()
	{
		return m_nReceivedSum.add(m_nSpendSum.negate());
	}
	
	public BigDecimal getFullDelta()
	{
		BigDecimal nDelta = getDelta();
		
		if (getNeedSellVolume().compareTo(BigDecimal.ZERO) != 0)
		{
			final BigDecimal nNeedSellVolume = calculateCriticalPrice().multiply(getNeedSellVolume());
			nDelta =  nDelta.add(nNeedSellVolume);
		}

		if (getPriviousLossSum().compareTo(BigDecimal.ZERO) != 0)
			nDelta =  nDelta.add(getPriviousLossSum().negate());

		return nDelta;
	}	
	
	public Integer getRuleID()
	{
		if (null == m_nRuleID)
			m_nRuleID = (int)(Math.random() * 100000);
		
		return m_nRuleID;
	}
	
	public TradeHistory getHistory()
	{
		if (null == m_oHistory)
			m_oHistory = new TradeHistory(getRuleID(), "trade_" + m_oRateInfo);
		
		return m_oHistory;
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
		return (null != m_nCriticalPrice ? m_nCriticalPrice : BigDecimal.ZERO);
	}
	
	public BigDecimal getPriviousLossSum()
	{
		if (null == m_nPriviousLossSum)
			m_nPriviousLossSum = BigDecimal.ZERO;
		return m_nPriviousLossSum;
	}
	
	public BigDecimal getMinCriticalPrice()
	{
		final BigDecimal nTradeMargin = TradeUtils.getMarginValue(getAveragedBoughPrice());
		final BigDecimal nLostPriceAddition = MathUtils.getBigDecimal(getPriviousLossSum().doubleValue() / getBoughtVolume().doubleValue(), TradeUtils.getPricePrecision(m_oRateInfo));
		final BigDecimal nFullAddition = nTradeMargin.add(nLostPriceAddition);
		return getAveragedBoughPrice().add(nFullAddition);
	}

	public BigDecimal calculateCriticalPrice()
	{
		final BigDecimal nTradeCommision = TradeUtils.getCommisionValue(getAveragedBoughPrice(), BigDecimal.ZERO);
		return getMinCriticalPrice().add(nTradeCommision);
	}
	
	public BigDecimal getCriticalVolume()
	{
		return (null != m_nCriticalVolume ? m_nCriticalVolume : BigDecimal.ZERO);
	}
	
	public String getCriticalPriceString()
	{
		return MathUtils.toCurrencyStringEx2(getCriticalPrice());
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
		final Date oOrderDateCreate = m_oOrder.getCreated();
		
		m_oOrder = oOrder;
		
		if (null != oOrderDateCreate && !m_oOrder.isNull())
			m_oOrder.setCreated(oOrderDateCreate);
	}
	
	public void setNeedBoughtVolume(final BigDecimal nNeedBoughtVolume)
	{
		if (m_nNeedBoughtVolume.compareTo(nNeedBoughtVolume) != 0)
			addToHistory("Set need buy volume : " + MathUtils.toCurrencyStringEx2(nNeedBoughtVolume)); 
		m_nNeedBoughtVolume = nNeedBoughtVolume;
	}
	
	public void setTradeSum(BigDecimal nTradeSum)
	{
		if (m_nTradeSum.compareTo(nTradeSum) != 0)
			addToHistory("Set trade sum : " + MathUtils.toCurrencyStringEx2(nTradeSum)); 
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
		setCriticalPrice(nCriticalPrice, StringUtils.EMPTY); 
	}
	
	public void setCriticalPrice(BigDecimal nCriticalPrice, final String strMessage)
	{
		m_nCriticalPrice = TradeUtils.getRoundedCriticalPrice(m_oRateInfo, nCriticalPrice);
		addToHistory("Set critical price : " + MathUtils.toCurrencyStringEx2(nCriticalPrice)); 
		if (StringUtils.isNotBlank(strMessage))
			addToHistory(strMessage);
	}
	
	public void setCriticalVolume(BigDecimal nCriticalVolume)
	{
		m_nCriticalVolume = nCriticalVolume;
		addToHistory("Set critical volume : " + MathUtils.toCurrencyStringEx2(nCriticalVolume)); 
	}

	public void setPriviousLossSum(final BigDecimal nPriviousLossSum)
	{
		m_nPriviousLossSum = nPriviousLossSum;
		addToHistory("Set privious loss sum : " + MathUtils.toCurrencyStringEx2(m_nPriviousLossSum)); 
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
		System.out.println("Trade "  + m_oRateInfo + ". " + strMessage);
		getHistory().addToHistory(strMessage);
	}
	
	public BigDecimal trimSellPrice(final BigDecimal oSellPrice)
	{
		return (oSellPrice.compareTo(getCriticalPrice()) < 0 ? getCriticalPrice() : oSellPrice);
	}

	public boolean isMoreCriticalPrice(final BigDecimal oSellPrice)
	{
		return (oSellPrice.compareTo(getCriticalPrice()) > 0);
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
		return "Trade: " + MathUtils.toCurrencyStringEx2(getReceivedSum()) + "-" + MathUtils.toCurrencyStringEx2(getSpendSum()) + "=" + MathUtils.toCurrencyStringEx2(getDelta()) + "\r\n " + 
				"Buy: " + MathUtils.toCurrencyStringEx2(getAveragedBoughPrice()) + "/" + MathUtils.toCurrencyStringEx2(getBoughtVolume()) + "\r\n " +
				"Sell: " + MathUtils.toCurrencyStringEx2(getAveragedSoldPrice()) + "/" + MathUtils.toCurrencyStringEx2(getSoldVolume());
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		String strResult = StringUtils.EMPTY;
		strResult += m_oRateInfo + "\r\n";
		
		strResult += "ReceivedSum: " + MathUtils.toCurrencyStringEx2(getReceivedSum()) + "\r\n";
		strResult += "SpendSum: " + MathUtils.toCurrencyStringEx2(getSpendSum()) + "\r\n";
		strResult += "BoughtVolume: " + MathUtils.toCurrencyStringEx2(getBoughtVolume()) + "\r\n";
		strResult += "SoldVolume: " + MathUtils.toCurrencyStringEx2(getSoldVolume()) + "\r\n";
		strResult += "PriviousLossSum: " + MathUtils.toCurrencyStringEx2(getPriviousLossSum()) + "\r\n";
		strResult += getHistory();
		return strResult;
	}
}

package solo.model.stocks.item.rules.task.trade;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.BaseObject;
import solo.utils.MathUtils;

public class TradesInfo extends BaseObject implements Serializable
{
	private static final long serialVersionUID = -7601846839785106296L;
	
	protected String m_strHistory = StringUtils.EMPTY;
	
	protected int m_nTradeCount = 0;
	protected BigDecimal m_nReceivedSum = BigDecimal.ZERO;
	protected BigDecimal m_nSpendSum = BigDecimal.ZERO;
	protected BigDecimal m_nSum = BigDecimal.ZERO;
	protected BigDecimal m_nBuySum = BigDecimal.ZERO;
	
	protected BigDecimal m_nBuyVolume = BigDecimal.ZERO;
	protected BigDecimal m_nSoldVolume = BigDecimal.ZERO;
	protected BigDecimal m_nVolume = BigDecimal.ZERO;
	
	public BigDecimal getDelta()
	{
		return m_nReceivedSum.add(m_nSpendSum.negate());
	}
	
	public String getHistory()
	{
		return m_strHistory;
	}
	
	public BigDecimal getSpendSum()
	{
		return m_nSpendSum;
	}
	
	public BigDecimal getReceivedSum()
	{
		return m_nReceivedSum;
	}
	
	public BigDecimal getSum()
	{
		return m_nSum;
	}
	
	public void setSum(final BigDecimal nSum, final Integer nMaxTrades)
	{
		m_nSum = nSum;
		m_nBuySum = MathUtils.getRoundedBigDecimal(nSum.doubleValue() / nMaxTrades, TradeUtils.DEFAULT_PRICE_PRECISION); 
	}
	
	public BigDecimal getBuySum()
	{
		return m_nBuySum;
	}
	
	public BigDecimal getSoldVolume()
	{
		return m_nSoldVolume;
	}
	
	public BigDecimal getBuyVolume()
	{
		return m_nBuyVolume;
	}
	
	public BigDecimal getVolume()
	{
		return m_nVolume;
	}
	
	public Integer getTradeCount()
	{
		return m_nTradeCount;
	}
	
	public void addBuy(BigDecimal nSpendSum, BigDecimal nBuyVolume)
	{
		if (nSpendSum.compareTo(BigDecimal.ZERO) == 0 && nBuyVolume.compareTo(BigDecimal.ZERO) == 0)
			return;
		
		m_nSpendSum = m_nSpendSum.add(nSpendSum);
		m_nSum = m_nSum.add(nSpendSum.negate());

		m_nBuyVolume = m_nBuyVolume.add(nBuyVolume);
		m_nVolume = m_nVolume.add(nBuyVolume);
		
		addToHistory("Buy : " + MathUtils.toCurrencyString(nSpendSum) + " / " + MathUtils.toCurrencyStringEx(nBuyVolume)); 
	}
	
	public void addSell(BigDecimal nReceivedSum, BigDecimal nSoldVolume)
	{
		if (nReceivedSum.compareTo(BigDecimal.ZERO) == 0 && nSoldVolume.compareTo(BigDecimal.ZERO) == 0)
			return;

		m_nReceivedSum = m_nReceivedSum.add(nReceivedSum);
		m_nSum = m_nSum.add(nReceivedSum);

		m_nSoldVolume = m_nSoldVolume.add(nSoldVolume);
		m_nVolume = m_nVolume.add(nSoldVolume.negate());
		
		addToHistory("Sell : " + MathUtils.toCurrencyString(nReceivedSum) + " / " + MathUtils.toCurrencyStringEx(nSoldVolume)); 
	}
	
	public void incTradeCount()
	{
		m_nTradeCount++;
	}
	
	protected void addToHistory(final String strMessage)
	{
		m_strHistory += strMessage + "\r\n";
	}
	
	protected void clearHistory()
	{
		m_strHistory = StringUtils.EMPTY;
	}
	
	public String getInfo()
	{
		return  "Count: " + getTradeCount() + "\r\n" + 
				"Money: " + MathUtils.toCurrencyString(getSum()) + " / " + MathUtils.toCurrencyStringEx(getVolume()) + "\r\n" + 
				"Trades: " + MathUtils.toCurrencyString(getReceivedSum()) + "-" + MathUtils.toCurrencyString(getSpendSum()) + "=" + MathUtils.toCurrencyString(getDelta());
	}
}

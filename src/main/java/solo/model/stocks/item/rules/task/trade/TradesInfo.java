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
	
	public void setSum(final BigDecimal nSum)
	{
		m_nSum = nSum;
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
	
	public void addSpendSum(BigDecimal nSpendSum)
	{
		if (nSpendSum.compareTo(BigDecimal.ZERO) == 0)
			return;
		
		m_nSpendSum = m_nSpendSum.add(nSpendSum);
		m_nSum = m_nSum.add(nSpendSum.negate());
		addToHistory("Add spend sum : " + MathUtils.toCurrencyString(nSpendSum)); 
	}
	
	public void addReceivedSum(BigDecimal nReceivedSum)
	{
		if (nReceivedSum.compareTo(BigDecimal.ZERO) == 0)
			return;

		m_nReceivedSum = m_nReceivedSum.add(nReceivedSum);
		m_nSum = m_nSum.add(nReceivedSum);
		addToHistory("Add received sum : " + MathUtils.toCurrencyString(nReceivedSum)); 
	}
	
	public void addSoldVolume(BigDecimal nSoldVolume)
	{
		if (nSoldVolume.compareTo(BigDecimal.ZERO) == 0)
			return;
		
		m_nSoldVolume = m_nSoldVolume.add(nSoldVolume);
		m_nVolume = m_nVolume.add(nSoldVolume.negate());
		addToHistory("Add sold volume : " + MathUtils.toCurrencyString(nSoldVolume)); 
	}
	
	public void addBuyVolume(BigDecimal nBuyVolume)
	{
		if (nBuyVolume.compareTo(BigDecimal.ZERO) == 0)
			return;

		m_nBuyVolume = m_nBuyVolume.add(nBuyVolume);
		m_nVolume = m_nVolume.add(nBuyVolume);
		addToHistory("Add buy volume : " + MathUtils.toCurrencyString(nBuyVolume)); 
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
				"Money: " + getSum() + " / " + getVolume() + "\r\n" + 
				"Trades: " + MathUtils.toCurrencyString(getReceivedSum()) + "-" + MathUtils.toCurrencyString(getSpendSum()) + "=" + MathUtils.toCurrencyString(getDelta());
	}
}

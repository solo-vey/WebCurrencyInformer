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
	
	public Integer getTradeCount()
	{
		return m_nTradeCount;
	}
	
	public void addSpendSum(BigDecimal nSpendSum)
	{
		if (nSpendSum.compareTo(BigDecimal.ZERO) == 0)
			return;
		
		m_nSpendSum = m_nSpendSum.add(nSpendSum);
		addToHistory("Add spend sum : " + MathUtils.toCurrencyString(nSpendSum)); 
	}
	
	public void addReceivedSum(BigDecimal nReceivedSum)
	{
		if (nReceivedSum.compareTo(BigDecimal.ZERO) == 0)
			return;

		m_nReceivedSum = m_nReceivedSum.add(nReceivedSum);
		addToHistory("Add received sum : " + MathUtils.toCurrencyString(nReceivedSum)); 
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
				"Trade: " + MathUtils.toCurrencyString(getReceivedSum()) + "-" + MathUtils.toCurrencyString(getSpendSum()) + "=" + MathUtils.toCurrencyString(getDelta());
	}
}

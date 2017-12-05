package solo.model.currency;

import java.math.BigDecimal;

import solo.model.stocks.BaseObject;

/** Информация о курсе валют  */
public class CurrencyAmount extends BaseObject
{
	final private BigDecimal m_nBalance; 
	final private BigDecimal m_nLocked; 
	
	public CurrencyAmount(final BigDecimal nBalance, final BigDecimal nLocked)
	{
		m_nBalance = nBalance;
		m_nLocked = nLocked;
	}
	
	public BigDecimal getBalance()
	{
		return m_nBalance;
	}
	
	public BigDecimal getLocked()
	{
		return m_nLocked;
	}
}

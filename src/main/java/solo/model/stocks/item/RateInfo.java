package solo.model.stocks.item;

import java.io.Serializable;

import solo.model.currency.Currency;

public class RateInfo implements Serializable
{
	private static final long serialVersionUID = -7207375688298563812L;
	
	public static RateInfo ETH_UAH = new RateInfo(Currency.ETH, Currency.UAH); 

	final protected Currency m_oCurrencyFrom; 
	final protected Currency m_oCurrencyTo;
	final protected boolean m_bIsReverse;
	
	public RateInfo(final Currency oCurrencyFrom, final Currency oCurrencyTo)
	{
		this(oCurrencyFrom, oCurrencyTo, false);
	}
	
	public RateInfo(final Currency oCurrencyFrom, final Currency oCurrencyTo, final boolean bIsReverse)
	{
		m_oCurrencyFrom = oCurrencyFrom;
		m_oCurrencyTo = oCurrencyTo;
		m_bIsReverse = bIsReverse;
	}
	
	public Currency getCurrencyFrom()
	{
		return m_oCurrencyFrom;
	}
	
	public Currency getCurrencyTo()
	{
		return m_oCurrencyTo;
	}
	
	public boolean getIsReverse()
	{
		return m_bIsReverse;
	}
	
	public static RateInfo getReverseRate(final RateInfo oRateInfo)
	{
		return new RateInfo(oRateInfo.getCurrencyTo(), oRateInfo.getCurrencyFrom(), !oRateInfo.getIsReverse());
	}
	
	@Override public boolean equals(Object oObject)
	{
		if (null == oObject)
			return false;
		
		if (!(oObject instanceof RateInfo))
			return false;
		
		final RateInfo oRateInfo = (RateInfo)oObject;
		return m_oCurrencyFrom.equals(oRateInfo.getCurrencyFrom()) && m_oCurrencyTo.equals(oRateInfo.getCurrencyTo());
	};
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		return m_oCurrencyFrom.toString().toLowerCase() + m_oCurrencyTo.toString().toLowerCase();
	}
	
	@Override public int hashCode()
	{
		return toString().hashCode();
	}
}

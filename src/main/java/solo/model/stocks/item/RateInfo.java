package solo.model.stocks.item;

import java.io.Serializable;

import solo.model.currency.Currency;

public class RateInfo implements Serializable, Comparable<RateInfo>
{
	private static final long serialVersionUID = -7207375688298563812L;
	
	public static final RateInfo ETH_UAH = new RateInfo(Currency.ETH, Currency.UAH); 
	public static final RateInfo NULL = new RateInfo(Currency.UAH, Currency.UAH);

	protected final Currency m_oCurrencyFrom; 
	protected final Currency m_oCurrencyTo;
	protected final boolean m_bIsReverse;
	
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
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		return m_oCurrencyFrom.toString().toLowerCase() + m_oCurrencyTo.toString().toLowerCase();
	}
	
	@Override public int hashCode()
	{
		return toString().hashCode();
	}

	@Override public int compareTo(final RateInfo oRateInfo)
	{
		if (oRateInfo == null)
			return -1;
		
		return oRateInfo.toString().compareToIgnoreCase(toString());
	}
}

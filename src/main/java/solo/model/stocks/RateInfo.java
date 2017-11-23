package solo.model.stocks;

import solo.model.currency.Currency;

public class RateInfo
{
	final protected Currency m_oCurrencyFrom; 
	
	final protected Currency m_oCurrencyTo;
	
	public RateInfo(final Currency oCurrencyFrom, final Currency oCurrencyTo)
	{
		m_oCurrencyFrom = oCurrencyFrom;
		m_oCurrencyTo = oCurrencyTo;
	}
	
	public Currency getCurrencyFrom()
	{
		return m_oCurrencyFrom;
	}
	
	public Currency getCurrencyTo()
	{
		return m_oCurrencyTo;
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
		return m_oCurrencyFrom + "->" + m_oCurrencyTo;
	}
}

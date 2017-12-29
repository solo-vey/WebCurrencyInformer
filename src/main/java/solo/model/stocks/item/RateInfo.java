package solo.model.stocks.item;

import java.io.Serializable;

import solo.model.currency.Currency;

public class RateInfo implements Serializable
{
	private static final long serialVersionUID = -7207375688298563812L;

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
		return m_oCurrencyFrom.toString().toLowerCase() + m_oCurrencyTo.toString().toLowerCase();
	}
	
	@Override public int hashCode()
	{
		return toString().hashCode();
	}
}

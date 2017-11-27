package solo.model.stocks.item;

import java.math.BigDecimal;

import solo.model.currency.Currency;

public class StockCurrencyVolume
{
	final protected Currency m_oCurrency;
	protected BigDecimal m_oVolume;
	
	public StockCurrencyVolume(final Currency oCurrency, final double nVolume)
	{
		m_oCurrency = oCurrency;
		m_oVolume = new BigDecimal(nVolume);
	}
	
	public Currency getCurrency()
	{
		return m_oCurrency;
	}
	
	public BigDecimal getVolume()
	{
		return m_oVolume;
	}
	
	public void setVolume(final double nVolume)
	{
		m_oVolume = new BigDecimal(nVolume);
	}
}

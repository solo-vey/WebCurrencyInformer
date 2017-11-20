package solo.model.currency;

/** Информация о валюте  */
public class CurrencyInfo
{
	/** Валюта */
	final private Currency m_oCurrency;
	/** Последнне значение цены продажи валюты */
	private double m_nLastSell = 0.0;
	/** Последнне значение цены покупки валюты */
	private double m_nLastBuy = 0.0;
	
	/** Конструктор 
	 * @param oCurrency Валюта */
	public CurrencyInfo(final Currency oCurrency)
	{
		m_oCurrency = oCurrency; 
	}
	
	/** @return Валюта */
	public Currency getCurreny()
	{
		return m_oCurrency;
	}
	
	/** @param nLastSell Новое значение цены покупки валюты */
	public void setLastSell(final double nLastSell)
	{
		m_nLastSell = nLastSell;
	}
	
	/** @return Последнне значение цены продажи валюты */
	public double getLastSell()
	{
		return m_nLastSell;
	}
	
	/** @param nLastBuy Новое значение цены продажи валюты */
	public void setLastBuy(final double nLastBuy)
	{
		m_nLastBuy = nLastBuy;
	}
	
	/** @return Последнне значение цены покупки валюты */
	public double getLastBuy()
	{
		return m_nLastBuy;
	}
}

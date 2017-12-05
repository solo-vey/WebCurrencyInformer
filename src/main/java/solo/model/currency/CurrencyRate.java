package solo.model.currency;

import java.math.BigDecimal;
import solo.utils.MathUtils;

/** Информация о курсе валют  */
public class CurrencyRate
{
	/** Какую валюту покупаем/продаем */
	final private Currency m_oCurrencyFrom;
	/** В какой валюте покупаем/продаем */
	final private Currency m_oCurrencyTo;
	/** курс */
	final private Double m_nValue; 
	
	/** Конструктор 
	 * @param oCurrencyFrom Какую валюту покупаем/продаем 
	 * @param oCurrencyTo В какой валюте покупаем/продаем */
	public CurrencyRate(final Currency oCurrencyFrom, final Currency oCurrencyTo, final Double nValue)
	{
		m_oCurrencyFrom = oCurrencyFrom;
		m_oCurrencyTo = oCurrencyTo;
		m_nValue = nValue; 
	}
	
	/** Какую валюту покупаем/продаем */
	public Currency getCurrenyFrom()
	{
		return m_oCurrencyFrom;
	}
	
	/** В какой валюте покупаем/продаем */
	public Currency getCurrenyTo()
	{
		return m_oCurrencyTo;
	}
	
	/** Курс */
	public Double getValue()
	{
		return m_nValue;
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		return m_oCurrencyFrom + " = " + MathUtils.toCurrencyString(new BigDecimal(m_nValue));
	}
}

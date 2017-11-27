package solo.model.currency;

import java.util.Locale;

/** Список валют */
public enum Currency
{
	/** Биткоин */
	BTC("Bitcoin", Locale.US),
	/** Ethereum */
	ETH("Ethereum", Locale.US), 
	/** Гривна */
	UAH("Uah", Locale.forLanguageTag("UA-ua"));
	
	/** Имя валюты */
	final String m_strName;
	/** Локаль для вывода валюты */
	final Locale m_oLocale;
	
	/** Конструктор
	 * @param strName Имя валюты  */
	Currency(final String strName, final Locale oLocale)
	{
		m_strName = strName;
		m_oLocale = oLocale;
	}
	
	/** Локаль для вывода валюты */
	public final Locale getLocale()
	{
		return m_oLocale;
	}
}

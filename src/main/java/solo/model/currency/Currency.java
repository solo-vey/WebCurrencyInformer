package solo.model.currency;

/** Список валют */
public enum Currency
{
	/** Биткоин */
	BTC("Bitcoin"),
	/** Ethereum */
	ETH("Ethereum"), 
	/** Waves */
	WAVES("Waves"),
	/** доллары */
	USD("Usd"),
	/** Евро */
	EUR("Eur"),
	/** рубли */
	RUB("Rub"),
	/** Гривна */
	UAH("Uah");
	
	/** Имя валюты */
	final String m_strName;
	
	/** Конструктор
	 * @param strName Имя валюты  */
	Currency(final String strName)
	{
		m_strName = strName;
	}
}

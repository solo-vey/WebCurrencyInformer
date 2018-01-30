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
	/** Xrp */
	XRP("Xpr"),
	/** Dash */
	DASH("Dash"),
	/** Doge */
	DOGE("Doge"),
	/** Zec */
	ZEC("Zec"),
	/** Usdt */
	USDT("Usdt"),
	/** Xmr */
	XMR("Xmr"),
	/** Etc */
	ETC("Etc"),
	/** Kick */
	KICK("Kick"),
	/** Bch */
	BCH("Bch"),
	/** доллары */
	USD("Usd"),
	/** польские злотые */
	PLN("Pln"),
	/** литовские */
	LTC("Ltc"),
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

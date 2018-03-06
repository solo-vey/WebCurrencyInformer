package solo.model.currency;

/** Список валют */
public enum Currency
{
	/** Биткоин */
	BTC("Bitcoin"),
	/** Ethereum */
	ETH("Ethereum"), 
	/** Ethereum Classic */
	ETC("EthereumClassic"),
	/** Electroneum */
	ETN("Electroneum"), 
	/** Waves */
	WAVES("Waves"),
	/** Xrp */
	XRP("Xpr"),
	/** Dash */
	DASH("Dash"),
	/** Doge */
	DOGE("Doge"),
	/** Dot */
	DOT("Dot"),
	/** Zec */
	ZEC("Zec"),
	/** Usdt */
	USDT("Usdt"),
	/** Xmr */
	XMR("Xmr"),
	/** WSX */
	WSX("WSX"),
	/** Kick */
	KICK("Kick"),
	/** Bch */
	BCH("Bch"),
	/** OrmeusCoin */
	ORME("Orme"),	
	
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

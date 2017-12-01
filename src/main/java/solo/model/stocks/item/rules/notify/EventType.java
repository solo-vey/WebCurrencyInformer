package solo.model.stocks.item.rules.notify;

/** Тип события */
public enum EventType
{
	/** Продажа */
	SELL("Продажа"),
	/** Отслеживание продаж */
	SELLTRACE("Отслеживание продаж"),
	/** Покупка */
	BUY("Покупка"),
	/** Отслеживание покупок */
	BUYTRACE("Отслеживание покупок"),
	/** Сделки */
	TRADE("Сделки"),
	/** Отслеживание сделок */
	TRADETRACE("Отслеживание сделок");
	
	/** Тип */
	final String m_strName;
	
	/** Конструктор
	 * @param strName Тип в виде строки  */
	EventType(final String strName)
	{
		m_strName = strName;
	}
	
	/** Тип в виде строки */
	public final String getName()
	{
		return m_strName;
	}
}

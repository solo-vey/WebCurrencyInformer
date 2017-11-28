package solo.model.stocks.item;

/** Тип события */
public enum EventType
{
	/** Продажа */
	SELL("Продажа"),
	/** Покупка */
	BUY("Покупка");
	
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

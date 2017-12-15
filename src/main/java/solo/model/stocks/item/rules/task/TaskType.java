package solo.model.stocks.item.rules.task;

/** Тип задания */
public enum TaskType
{
	/** Защита ордеров */
	PROTECTORDERS("Защита ордеров"),
	/** Быстрая торговля */
	QUICKTRADE("Быстрая торговля"),
	/** Быстрая продажа */
	QUICKSELL("Быстрая продажа"),
	/** Быстрая покупка */
	QUICKBUY("Быстрая покупка");
	
	/** Тип */
	final String m_strName;
	
	/** Конструктор
	 * @param strName Тип в виде строки  */
	TaskType(final String strName)
	{
		m_strName = strName;
	}
	
	/** Тип в виде строки */
	public final String getName()
	{
		return m_strName;
	}
}

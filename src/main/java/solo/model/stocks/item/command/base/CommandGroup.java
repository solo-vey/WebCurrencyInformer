package solo.model.stocks.item.command.base;

/** Тип комманды */
public enum CommandGroup
{
	/** Правила */
	RULES("Правила"),
	INFO("Информация"),
	TRADE("Торговля"),
	OTHER("Другие"),
	SYSTEM("Системные");
	
	final String m_strName;
	
	CommandGroup(final String strName)
	{
		m_strName = strName;
	}
	
	public final String getName()
	{
		return m_strName;
	}
}

package solo.model.stocks.item.rules.task.trade;

public enum ControlerSubState
{
	/** Есть ошибки при работе */
	ERROR,
	/** Работает нормально */
	WORK,
	/** Переходит в состояние ожидания, но есть незакрытые */
	GOTO_WAIT,
	/** Ждет разршения начать работать */
	WAIT,
	/** Остановлен */
	STOPPING,
	/** Остановлен */
	STOPPED;
	
	@Override public String toString()
	{
		return (WORK.equals(this) ? super.toString().toLowerCase() : super.toString());
	}
}

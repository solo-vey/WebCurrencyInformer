package solo.model.stocks.item.rules.task.trade;

public enum ControlerState
{
	/** Работает */
	WORK,
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

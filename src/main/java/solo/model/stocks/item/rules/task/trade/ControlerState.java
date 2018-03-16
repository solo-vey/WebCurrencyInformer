package solo.model.stocks.item.rules.task.trade;

public enum ControlerState
{
	/** Работает */
	WORK,
	/** Работает но есть проблемы */
	WORK_ERROR,
	/** Ждет разршения начать работать */
	WAIT,
	/** Переходит в состояние - ждать начала работы  */
	GOTO_WAIT,
	/** Останавливается полностью */
	STOPPING,
	/** Остановлен */
	STOPPED;
	
	public boolean isWork()
	{
		return ControlerState.WORK.equals(this) || ControlerState.WORK_ERROR.equals(this);
	}
	
	public boolean isWait()
	{
		return ControlerState.WAIT.equals(this) || ControlerState.GOTO_WAIT.equals(this);
	}
	
	public boolean isStop()
	{
		return isStopping() || isStopped();
	}
	
	public boolean isStopping()
	{
		return ControlerState.STOPPING.equals(this);
	}
	
	public boolean isStopped()
	{
		return ControlerState.STOPPED.equals(this);
	}
	
	@Override public String toString()
	{
		if (ControlerState.WORK.equals(this))
			return super.toString().toLowerCase();
		
		if (ControlerState.WORK_ERROR.equals(this))
			return "ERROR";
		
		if (ControlerState.GOTO_WAIT.equals(this))
			return "Wait";
			
		return super.toString();
	}
}

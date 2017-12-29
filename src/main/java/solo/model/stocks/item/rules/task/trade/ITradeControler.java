package solo.model.stocks.item.rules.task.trade;

public interface ITradeControler
{
	final static ITradeControler NULL = new NullTradeControler();
	
	void tradeDone(final TaskTrade oTaskTrade);
	void buyDone(final TaskTrade oTaskTrade);
}

class NullTradeControler implements ITradeControler
{
	public void tradeDone(final TaskTrade oTaskTrade) 
	{
	}

	public void buyDone(final TaskTrade oTaskTrade) 
	{
	}
}

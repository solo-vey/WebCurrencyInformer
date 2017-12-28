package solo.model.stocks.item.rules.task.trade;

public interface ITradeControler
{
	final static ITradeControler NULL = new NullTradeControler();
	
	void tradeDone(final TaskTrade oTaskTrade);
}

class NullTradeControler implements ITradeControler
{
	public void tradeDone(final TaskTrade oTaskTrade) 
	{
	}
}

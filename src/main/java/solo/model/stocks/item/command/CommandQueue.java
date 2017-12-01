package solo.model.stocks.item.command;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class CommandQueue
{
	final protected Queue<ICommand> m_oCommandQueue = new LinkedBlockingQueue<ICommand>();
	
	public void addCommand(final ICommand oCommand)
	{
		m_oCommandQueue.add(oCommand);
	}
	
	public ICommand getNextCommand()
	{
		return m_oCommandQueue.poll();
	}
	
	public int size()
	{
		return m_oCommandQueue.size();
	}
}

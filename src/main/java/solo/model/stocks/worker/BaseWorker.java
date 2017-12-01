package solo.model.stocks.worker;

import java.util.Date;

import solo.model.stocks.item.command.CommandFactory;
import solo.model.stocks.item.command.CommandQueue;
import solo.model.stocks.item.command.ICommand;

public class BaseWorker extends Thread implements IWorker
{
	final protected CommandQueue m_oCommandQueue = new CommandQueue();
	
	protected boolean m_bIsManualStopped = false;
	final protected int m_nTimeOut;
	
	public BaseWorker()
	{
		this(1000);
	}
	
	public BaseWorker(final int nTimeOut)
	{
		m_nTimeOut = nTimeOut;
	}
	
	public void addCommand(final ICommand oCommand)
	{
		m_oCommandQueue.addCommand(oCommand);
	}
	
	public void run()
	{
		while (!m_bIsManualStopped)
		{
			try
			{
				doWork();
				Thread.sleep(m_nTimeOut);
			}
			catch (Exception e) 
			{
				System.err.printf("Thread exception : " + e + "\r\n");
			}
		}
	}
	
	protected void doWork() throws Exception
	{
		while (!m_bIsManualStopped)
		{
			final ICommand oCommand = m_oCommandQueue.getNextCommand();
			if (null == oCommand)
				return;
			
			oCommand.execute();
			System.err.printf(Thread.currentThread().getName() +  " Execute command [" + CommandFactory.getCommandName(oCommand.getClass()) + "] complete. " + (new Date()) + " Info [" + oCommand.getInfo() + "]. Queue size [" + m_oCommandQueue.size() + "]\r\n");
		}
	}
	
	public void startWorker()
	{
		start();
	}
	
	public void stopWorker()
	{
		m_bIsManualStopped = true;
	}
}

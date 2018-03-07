package solo.model.stocks.worker;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.command.base.CommandQueue;
import solo.model.stocks.item.command.base.ICommand;
import solo.model.stocks.item.command.rule.GetRulesCommand;

public class BaseWorker extends Thread implements IWorker
{
	final protected CommandQueue m_oCommandQueue = new CommandQueue();
	
	protected boolean m_bIsManualStopped = true;
	final protected int m_nTimeOut;
	final protected Stocks m_oStock;
	
	public BaseWorker()
	{
		this(1000, Stocks.Uknown);
	}
	
	public BaseWorker(final int nTimeOut, final Stocks oStock)
	{
		m_nTimeOut = nTimeOut;
		m_oStock = oStock;
	}
	
	public Stocks getStock()
	{
		return m_oStock;
	}
	
	public void addCommand(final ICommand oCommand)
	{
		m_oCommandQueue.addCommand(oCommand);
	}
	
	public void run()
	{
		System.out.println(Thread.currentThread().getName() +  " Start");
		
		while (!m_bIsManualStopped)
		{
			try
			{
				doWork();
				Thread.sleep(getTimeOut());
			}
			catch (Exception e) 
			{
				WorkerFactory.onException(StringUtils.EMPTY, e);
			}
		}
		
		System.out.println(Thread.currentThread().getName() +  " Stoped");
	}

	int getTimeOut()
	{
		return m_nTimeOut;
	}
	
	protected void doWork() throws Exception
	{
		while (!m_bIsManualStopped)
		{
			final ICommand oCommand = m_oCommandQueue.getNextCommand();
			if (null == oCommand)
				return;

			if (oCommand instanceof GetRulesCommand)
			{
				int i = 0;
				i++;
			}
			
			oCommand.execute();
//			System.err.printf(Thread.currentThread().getName() +  "[" + getStock() + "] Execute command [" + CommandFactory.getCommandName(oCommand.getClass()) + "] complete. " + (new Date()) + " Info [" + oCommand.getInfo() + "]. Queue size [" + m_oCommandQueue.size() + "]\r\n");
		}
	}
	
	public void startWorker()
	{
		m_bIsManualStopped = false;
		start();
	}
	
	public void stopWorker()
	{
		m_bIsManualStopped = true;
	}
	
	public boolean isWork()
	{
		return !m_bIsManualStopped;
	}
}

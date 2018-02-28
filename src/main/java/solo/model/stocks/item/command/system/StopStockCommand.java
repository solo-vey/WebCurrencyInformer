package solo.model.stocks.item.command.system;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;

public class StopStockCommand extends BaseCommand
{
	final static public String NAME = "stop";
	
	public StopStockCommand(final String strСommandLine)
	{
		super(strСommandLine, "#type#");
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final String strType = getParameter("#type#").toLowerCase();

		if (strType.equalsIgnoreCase("all"))
		{
			for(final MainWorker oMainWorker : WorkerFactory.getAllMainWorkers().values())
				oMainWorker.getStockWorker().stopWorker();
				
			WorkerFactory.getMainWorker().sendSystemMessage("All stocks stopping");
		}
		else
		{
			WorkerFactory.getMainWorker().getStockWorker().stopWorker();
			WorkerFactory.getMainWorker().sendSystemMessage("Stock stopping");
		}
	}
}

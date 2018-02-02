package solo.model.stocks.item.command.system;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

public class StartStockCommand extends BaseCommand
{
	final static public String NAME = "start";
	
	public StartStockCommand(final String strСommandLine)
	{
		super(strСommandLine, StringUtils.EMPTY);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		WorkerFactory.getMainWorker().getStockWorker().startWorker();
		
		WorkerFactory.getMainWorker().addCommand(new SendMessageCommand("Stock starting"));
	}
}

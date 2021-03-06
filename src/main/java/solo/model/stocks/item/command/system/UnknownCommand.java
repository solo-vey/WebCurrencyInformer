package solo.model.stocks.item.command.system;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class UnknownCommand extends BaseCommand implements ISystemCommand
{
	public UnknownCommand(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		WorkerFactory.getMainWorker().sendSystemMessage(getInfo());
	}
}

package solo.model.stocks.item.command.trade;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class GetManagerInfoCommand extends BaseCommand
{
	final static public String NAME = "manager";
	final static public String TYPE_PARAMETER = "#type#";
	
	public GetManagerInfoCommand(final String strСommandLine)
	{
		super(strСommandLine, TYPE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final String strType = getParameter(TYPE_PARAMETER);
		
		final String strMessage = WorkerFactory.getStockExchange().getManager().getInfo().asString(strType);
		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}
}

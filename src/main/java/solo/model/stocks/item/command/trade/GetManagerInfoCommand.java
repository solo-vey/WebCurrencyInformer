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
		
		final String strMessage = WorkerFactory.getStockExchange().getManager().getInfo().asString(strType) +
			"BUTTONS\r\n[{\"text\":\"Days\",\"callback_data\":\"manager_days\"},{\"text\":\"Hours\",\"callback_data\":\"manager_hours\"}," +
			"{\"text\":\"Months\",\"callback_data\":\"manager_months\"}, {\"text\":\"All\",\"callback_data\":\"manager\"}]";
		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}
}

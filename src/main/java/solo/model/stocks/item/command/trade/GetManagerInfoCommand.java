package solo.model.stocks.item.command.trade;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.telegram.TelegramTransport;

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
	
	@SuppressWarnings("unchecked")
	public void execute() throws Exception
	{
		super.execute();
		
		final String strType = getParameter(TYPE_PARAMETER);
		
		String strMessage = StringUtils.EMPTY;
		if (strType.equalsIgnoreCase("HISTORY"))
			strMessage = WorkerFactory.getStockExchange().getManager().getHistory().toString();
		else
			strMessage = WorkerFactory.getStockExchange().getManager().getInfo().asString(strType);
		
		strMessage += "BUTTONS\r\n" + TelegramTransport.getButtons(Arrays.asList(Arrays.asList("Days=manager_days", "Hours=manager_hours", "Months=manager_months", "All=manager"),
																		Arrays.asList("Last24H=manager_last24hours", "RateLast24H=manager_ratelast24hours", "RateLastHours=manager_ratelast24forhours", "History=manager_history"),
																		Arrays.asList("Parameters=setstockparameter_?")));
		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}
}

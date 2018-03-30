package solo.model.stocks.item.command.trade;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.rules.task.manager.IStockManager;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.item.rules.task.manager.StockManager;
import solo.model.stocks.item.rules.task.trade.ControlerState;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
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
		
		final IStockManager oManager = WorkerFactory.getStockExchange().getManager();
		final String strType = getParameter(TYPE_PARAMETER);
		
		String strMessage = StringUtils.EMPTY;
		if (strType.equalsIgnoreCase("STOP"))
		{
			oManager.setOperations(StockManager.OPERATIONS_DEFAULT);
			strMessage = "Manager uses only [" + StockManager.OPERATIONS_DEFAULT + "]";
		}
		else
		if (strType.equalsIgnoreCase("START"))
		{
			oManager.setOperations(StockManager.OPERATIONS_ALL);
			strMessage = "Manager uses all operations";
		}
		else
		if (strType.toUpperCase().startsWith("SYNCHONIZE:"))
		{
			strMessage = oManager.getMoney().synchonize(strType.toUpperCase().replace("SYNCHONIZE:", StringUtils.EMPTY));
		}		
		else
		if (strType.equalsIgnoreCase("HISTORY"))
			strMessage = oManager.getHistory().toString();
		else
		if (strType.equalsIgnoreCase("INFO"))
			strMessage = getInfo(oManager);
		else
			strMessage = oManager.getInfo().asString(strType);
		
		final String strOperationCommand = (oManager.getOperations().equalsIgnoreCase(StockManager.OPERATIONS_DEFAULT) ? "Start=manager_start" : "Stop=manager_stop");
		strMessage += "BUTTONS\r\n" + TelegramTransport.getButtons(Arrays.asList(Arrays.asList("Days=manager_days", "Hours=manager_hours", "Months=manager_months", "All=manager"),
																		Arrays.asList("Last24H=manager_last24hours", "RateLast24H=manager_ratelast24hours", "RateLastHours=manager_ratelasthours", "History=manager_history"),
																		Arrays.asList(strOperationCommand, "Info=manager_info", "Parameters=setstockparameter_?", "Stocks=setCurrentStock"),
																		Arrays.asList("Synchonize Info=manager_synchonize:info", "Synchonize=manager_synchonize:synchronize")));
		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}

	protected String getInfo(final IStockManager oManager)
	{
		String strMessage;
		strMessage = "<code>Rates [" + WorkerFactory.getStockSource().getRates().size() + "]</code>\r\n";
		for(final RateInfo oRateInfo : WorkerFactory.getStockSource().getRates())
		{
			final BigDecimal nAverageRateProfitabilityPercent = ManagerUtils.getAverageRateProfitabilityPercent(oRateInfo); 
			strMessage += "<code>" + oRateInfo + "[" + nAverageRateProfitabilityPercent + "%],</code> ";
		}
		strMessage += "\r\n\r\n";
		
		strMessage += "<code>Controlers</code>\r\n";
		final Map<ControlerState, Integer> oControlers = new HashMap<ControlerState, Integer>();
		for(final Entry<Integer, IRule> oRule : WorkerFactory.getStockExchange().getRules().getRules().entrySet())
		{
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule.getValue());
			if (null == oControler || ManagerUtils.isTestObject(oControler))
				continue;
			
			if (!oControlers.containsKey(oControler.getControlerState()))
				oControlers.put(oControler.getControlerState(), 0);
			oControlers.put(oControler.getControlerState(), oControlers.get(oControler.getControlerState()) + 1);
			
			final BigDecimal nAverageRateProfitabilityPercent = ManagerUtils.getAverageRateProfitabilityPercent(oControler.getTradesInfo().getRateInfo()); 
			if (nAverageRateProfitabilityPercent.compareTo(BigDecimal.ZERO) < 0)
				strMessage += "<code>" + oRule.getValue().getInfo().toUpperCase() + "[" + nAverageRateProfitabilityPercent + "%]</code>\r\n";
			else
				strMessage += "<code>" + oRule.getValue().getInfo() + "[" + nAverageRateProfitabilityPercent + "%]</code>\r\n";
		}
		strMessage += "\r\n\r\n";
		return strMessage;
	}
}

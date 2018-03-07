package solo.model.stocks.item.command.trade;

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
import solo.model.stocks.item.rules.task.manager.TradesBlock;
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
		if (strType.equalsIgnoreCase("HISTORY"))
			strMessage = oManager.getHistory().toString();
		else
		if (strType.equalsIgnoreCase("INFO"))
			strMessage = getInfo(oManager);
		else
			strMessage = oManager.getInfo().asString(strType);
		
		strMessage += "BUTTONS\r\n" + TelegramTransport.getButtons(Arrays.asList(Arrays.asList("Days=manager_days", "Hours=manager_hours", "Months=manager_months", "All=manager"),
																		Arrays.asList("Last24H=manager_last24hours", "RateLast24H=manager_ratelast24hours", "RateLastHours=manager_ratelast24forhours", "History=manager_history"),
																		Arrays.asList("Info=manager_info", "Parameters=setstockparameter_?", "Stocks=setCurrentStock")));
		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}

	protected String getInfo(final IStockManager oManager)
	{
		String strMessage;
		strMessage = "<code>Rates [" + WorkerFactory.getStockSource().getRates().size() + "]</code>\r\n";
		for(final RateInfo oRateInfo : WorkerFactory.getStockSource().getRates())
		{
			final TradesBlock oRateInfoTradesBlock = oManager.getInfo().getRateLast24Hours().getTotal().getRateTrades().get(oRateInfo);
			final String strPercent = (null != oRateInfoTradesBlock ? oRateInfoTradesBlock.getPercent() + "%" : "?"); 
			strMessage += "<code>" + oRateInfo + "[" + strPercent + "],</code> ";
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
			
			final TradesBlock oRateInfoTradesBlock = oManager.getInfo().getRateLast24Hours().getTotal().getRateTrades().get(oControler.getTradesInfo().getRateInfo());
			final String strPercent = (null != oRateInfoTradesBlock ? oRateInfoTradesBlock.getPercent() + "%" : "?");
			if (strPercent.contains("-"))
				strMessage += "<code>" + oRule.getValue().getInfo().toUpperCase() + "[" + strPercent + "]</code>\r\n";
			else
				strMessage += "<code>" + oRule.getValue().getInfo() + "[" + strPercent + "]</code>\r\n";
		}
		strMessage += "\r\n\r\n";
		return strMessage;
	}
}

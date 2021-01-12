package solo.model.stocks.item.command.system;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.telegram.TelegramTransport;

public class SetCurrentStockCommand extends BaseCommand
{
	public static final String NAME = "setCurrentStock";

	public static final String STOCK_PARAMETER = "#stock#";

	public SetCurrentStockCommand(final String strСommandLine)
	{
		super(strСommandLine, STOCK_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		final String strStock = getParameter(STOCK_PARAMETER);
		
		super.execute();
		
		if (StringUtils.isNotBlank(strStock))
			WorkerFactory.setCurrentMainWorker(strStock);
		
	   	final List<List<String>> aButtons = new LinkedList<List<String>>();
		final Stocks oCurentStock = WorkerFactory.getCurrentSock();
		for(final Stocks oStock : Stocks.values())
		{
			if (WorkerFactory.isStockActive(oStock) && !oStock.equals(oCurentStock))
				aButtons.add(Arrays.asList(oStock + "=" + CommandFactory.makeCommandLine(SetCurrentStockCommand.class, SetCurrentStockCommand.STOCK_PARAMETER, oStock)));
		}
		
		WorkerFactory.getMainWorker().sendSystemMessage("Current stock [" + oCurentStock + "]" + "BUTTONS\r\n" + TelegramTransport.getButtons(aButtons));	
	}
}

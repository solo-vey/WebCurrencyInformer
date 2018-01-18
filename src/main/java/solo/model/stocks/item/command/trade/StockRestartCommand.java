package solo.model.stocks.item.command.trade;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class StockRestartCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "stockRestart";
	
	public StockRestartCommand(final String strСommandLine)
	{
		super(strСommandLine, StringUtils.EMPTY);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		WorkerFactory.getStockExchange().getStockSource().restart();
		WorkerFactory.getMainWorker().sendMessage("Stock restart complete");
	}
}

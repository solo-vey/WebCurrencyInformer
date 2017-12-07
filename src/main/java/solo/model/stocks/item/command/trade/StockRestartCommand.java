package solo.model.stocks.item.command.trade;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;

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
		getStockExchange().getStockSource().restart();
		sendMessage("Stock restart complete");
	}
}

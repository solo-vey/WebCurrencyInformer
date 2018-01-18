package solo.model.stocks.item.command.trade;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.ISystemCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class ManageStock extends BaseCommand implements ISystemCommand
{
	final static public String NAME = "manageStock";

	public ManageStock()
	{
		super(NAME, StringUtils.EMPTY);
	}

	public ManageStock(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
		final StateAnalysisResult oStateAnalysisResult = oStockExchange.getHistory().getLastAnalysisResult();
		oStockExchange.getManager().manage(oStateAnalysisResult);
	}
}

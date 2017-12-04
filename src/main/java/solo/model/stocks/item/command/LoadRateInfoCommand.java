package solo.model.stocks.item.command;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.history.StocksHistory;
import solo.model.stocks.item.StockRateStates;

/** Формат комманды 
 */
public class LoadRateInfoCommand extends BaseCommand
{
	final static public String NAME = "loadRate";
	
	public LoadRateInfoCommand()
	{
		super();
	}
	
	public LoadRateInfoCommand(final String strCommandLine)
	{
		super(strCommandLine);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = getStockExchange();
		final StockRateStates oRateStates = oStockExchange.getStockSource().getStockRates();
		final StateAnalysisResult oStateAnalysisResult = oStockExchange.getAnalysis().analyse(oRateStates, oStockExchange);
		oStockExchange.getHistory().addToHistory(oStateAnalysisResult);
		StocksHistory.addHistory(oStockExchange, oRateStates);
		
		final ICommand oCommand = new CheckRulesCommand();
		getMainWorker().addCommand(oCommand);
		
	}
}

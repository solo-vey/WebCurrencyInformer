package solo.model.stocks.item.command.system;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.history.StocksHistory;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.ICommand;
import solo.model.stocks.item.command.rule.CheckRulesCommand;

/** Формат комманды 
 */
public class LoadRateInfoCommand extends BaseCommand implements ISystemCommand
{
	final static public String NAME = "loadRate";
	
	public LoadRateInfoCommand()
	{
		super(NAME, StringUtils.EMPTY);
	}
	
	public LoadRateInfoCommand(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
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

package solo.model.stocks.item.command;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.history.StocksHistory;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;

/** Формат комманды 
 */
public class LoadRateInfoCommand extends BaseCommand
{
	final static public String NAME = "loadRate";

	final protected IStockExchange m_oStockExchange;
	
	public LoadRateInfoCommand(final String strStockName)
	{
		this(StockExchangeFactory.getStockExchange(strStockName));
	}
	
	public LoadRateInfoCommand(final IStockExchange oStockExchange)
	{
		super(oStockExchange.getStockName());
		m_oStockExchange = oStockExchange;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final StockRateStates oRateStates = m_oStockExchange.getStockSource().getStockRates();
		final StateAnalysisResult oStateAnalysisResult = m_oStockExchange.getAnalysis().analyse(oRateStates, m_oStockExchange);
		m_oStockExchange.getHistory().addToHistory(oStateAnalysisResult);
		StocksHistory.addHistory(m_oStockExchange, oRateStates);
		
		final ICommand oCommand = new CheckRulesCommand(m_oStockExchange);
		WorkerFactory.getWorker(WorkerType.STOCK).addCommand(oCommand);
		
	}
}

package solo.model.stocks.item.command.system;

import java.io.File;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class GetRateChartCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "chart";
	final static public String RATE_PARAMETER = "#rate#";

	final protected RateInfo m_oRateInfo;
	
	public GetRateChartCommand(final String strRateInfo)
	{
		super(strRateInfo, RATE_PARAMETER);
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = WorkerFactory.getStockExchange(); 
    	final Candlestick oCandlestick = oStockExchange.getStockCandlestick().get(m_oRateInfo);
    	final String strFileName = oCandlestick.makeChartImage(50);
    	
    	final StateAnalysisResult oStateAnalysisResult = oStockExchange.getLastAnalysisResult();
    	final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
    	final String strMessage = GetRateInfoCommand.getRateData(m_oRateInfo, oAnalysisResult);
    	
    	WorkerFactory.getTransport().sendPhoto(new File(strFileName), strMessage);
	}
}

package solo.model.stocks.item.command.system;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class GetRateInfoCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "getRate";
	final static public String RATE_PARAMETER = "#rate#";

	final protected RateInfo m_oRateInfo;
	
	public GetRateInfoCommand(final String strRateInfo)
	{
		super(strRateInfo, RATE_PARAMETER);
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = WorkerFactory.getStockExchange(); 
    	final StateAnalysisResult oStateAnalysisResult = oStockExchange.getHistory().getLastAnalysisResult();
    	
    	final List<RateInfo> aRates = (null != m_oRateInfo ? Arrays.asList(m_oRateInfo) : WorkerFactory.getStockSource().getRates());
    	
    	String strMessage = StringUtils.EMPTY;
    	for(final RateInfo oRateInfo : aRates)
    	{
			final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(oRateInfo);
			strMessage += getRateData(oRateInfo, oAnalysisResult);
			
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo);
			final RateAnalysisResult oReverseAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(oReverseRateInfo);
			strMessage += getRateData(oReverseRateInfo, oReverseAnalysisResult);
    	}
		
    	WorkerFactory.getMainWorker().sendMessage(strMessage);
	}

	protected String getRateData(final RateInfo oRateInfo, final RateAnalysisResult oAnalysisResult)
	{
		if (null == oAnalysisResult)
			return StringUtils.EMPTY;
		
		String strData = oRateInfo.toString() + "\r\n"; 
		strData += "Sell : " + MathUtils.toCurrencyStringEx2(oAnalysisResult.getAsksAnalysisResult().getBestPrice()) + 
							" / " + MathUtils.toCurrencyStringEx2(oAnalysisResult.getAsksAnalysisResult().getAverageAllSumPrice()) + "\r\n";   
		strData += "Buy : " + MathUtils.toCurrencyStringEx2(oAnalysisResult.getBidsAnalysisResult().getBestPrice()) + 
							" / " + MathUtils.toCurrencyStringEx2(oAnalysisResult.getBidsAnalysisResult().getAverageAllSumPrice()) + "\r\n";   
		strData += "Trades : " + MathUtils.toCurrencyStringEx2(oAnalysisResult.getTradesAnalysisResult().getBestPrice()) + 
							" / " + MathUtils.toCurrencyStringEx2(oAnalysisResult.getTradesAnalysisResult().getAverageAllSumPrice()) + "\r\n";
		strData += "Delta : " + MathUtils.toCurrencyStringEx2(oAnalysisResult.getAsksAnalysisResult().getBestPrice().add(oAnalysisResult.getBidsAnalysisResult().getBestPrice().negate())) + 
							" / " + MathUtils.toCurrencyStringEx2(oAnalysisResult.getAsksAnalysisResult().getAverageAllSumPrice().add(oAnalysisResult.getBidsAnalysisResult().getAverageAllSumPrice().negate())) + 
							" / " + MathUtils.toCurrencyStringEx2(TradeUtils.getCommisionValue(oAnalysisResult.getAsksAnalysisResult().getBestPrice(), oAnalysisResult.getBidsAnalysisResult().getBestPrice())
																	.add(TradeUtils.getMarginValue(oAnalysisResult.getAsksAnalysisResult().getBestPrice()))) + "\r\n\r\n";
		return strData;
	}
}

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
		
		final IStockExchange oStockExchange = getStockExchange(); 
    	final StateAnalysisResult oStateAnalysisResult = oStockExchange.getHistory().getLastAnalysisResult();
    	
    	final List<RateInfo> aRates = (null != m_oRateInfo ? Arrays.asList(m_oRateInfo) : getStockSource().getRates());
    	
    	String strMessage = StringUtils.EMPTY;
    	for(final RateInfo oRateInfo : aRates)
    	{
			final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(oRateInfo);
			strMessage += oRateInfo.toString() + "\r\n"; 
			strMessage += "Sell : " + MathUtils.toCurrencyString(oAnalysisResult.getAsksAnalysisResult().getBestPrice()) + 
								" / " + MathUtils.toCurrencyString(oAnalysisResult.getAsksAnalysisResult().getAverageAllSumPrice()) + "\r\n";   
			strMessage += "Buy : " + MathUtils.toCurrencyString(oAnalysisResult.getBidsAnalysisResult().getBestPrice()) + 
								" / " + MathUtils.toCurrencyString(oAnalysisResult.getBidsAnalysisResult().getAverageAllSumPrice()) + "\r\n";   
			strMessage += "Trades : " + MathUtils.toCurrencyString(oAnalysisResult.getTradesAnalysisResult().getBestPrice()) + 
								" / " + MathUtils.toCurrencyString(oAnalysisResult.getTradesAnalysisResult().getAverageAllSumPrice()) + "\r\n";
			strMessage += "Delta : " + MathUtils.toCurrencyString(oAnalysisResult.getAsksAnalysisResult().getBestPrice().add(oAnalysisResult.getBidsAnalysisResult().getBestPrice().negate())) + 
								" / " + MathUtils.toCurrencyString(oAnalysisResult.getAsksAnalysisResult().getAverageAllSumPrice().add(oAnalysisResult.getBidsAnalysisResult().getAverageAllSumPrice().negate())) + 
								" / " + MathUtils.toCurrencyString(
										TradeUtils.getCommisionValue(oAnalysisResult.getAsksAnalysisResult().getBestPrice(), oAnalysisResult.getBidsAnalysisResult().getBestPrice())
													.add(TradeUtils.getMarginValue(oAnalysisResult.getAsksAnalysisResult().getBestPrice()))) + "\r\n\r\n";
    	}
		
		sendMessage(strMessage);
	}
}

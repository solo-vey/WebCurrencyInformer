package solo.model.stocks.item.command.system;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class GetRateInfoCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "rate";
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
    	final StateAnalysisResult oStateAnalysisResult = oStockExchange.getLastAnalysisResult();
    	
    	final List<RateInfo> aRates = (null != m_oRateInfo ? Arrays.asList(m_oRateInfo) : WorkerFactory.getStockSource().getRates());
    	
    	String strMessage = StringUtils.EMPTY;
    	for(final RateInfo oRateInfo : aRates)
    	{
			final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(oRateInfo);
			strMessage += CommandFactory.makeCommandLine(GetRateChartCommand.class, GetRateChartCommand.RATE_PARAMETER, oRateInfo) + "\r\n"; 
			strMessage += getRateData(oRateInfo, oAnalysisResult) + "\r\n";
    	}
		
    	WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}

	public static String getRateData(final RateInfo oRateInfo, final RateAnalysisResult oAnalysisResult)
	{
		if (null == oAnalysisResult)
			return StringUtils.EMPTY;
		
		final BigDecimal nAskPrice = oAnalysisResult.getBestAskPrice();
		final BigDecimal nBidPrice = oAnalysisResult.getBestBidPrice();
		
		final BigDecimal nDelta = nAskPrice.add(nBidPrice.negate());
		final BigDecimal nCommisionAndMargin = TradeUtils.getCommisionValue(nAskPrice, nBidPrice).add(TradeUtils.getMarginValue(nAskPrice, oRateInfo));
		
		final BigDecimal nQuarterDelta = MathUtils.getBigDecimal(nDelta.doubleValue() / 4, TradeUtils.getPricePrecision(oRateInfo));
		final BigDecimal nAskBottomPrice = nAskPrice.add(nQuarterDelta.negate());
		final BigDecimal nBidTopPrice = nBidPrice.add(nQuarterDelta);
		final BigDecimal nTradePrice = oAnalysisResult.getTopTradePrice();
		final String strTradeType = (nTradePrice.compareTo(nAskBottomPrice) > 0 ? "^" : nTradePrice.compareTo(nBidTopPrice) < 0 ? "v" : "-");
		
		String strData = MathUtils.toCurrencyStringEx3(nAskPrice) + " / " +   
						MathUtils.toCurrencyStringEx3(nBidPrice) + " / " +  
						MathUtils.toCurrencyStringEx3(nTradePrice) + "[" + strTradeType + "]\r\n";
		strData += MathUtils.toCurrencyStringEx3(nDelta) + " / " + 
					MathUtils.toCurrencyStringEx3(nCommisionAndMargin) + " / " + 
					MathUtils.toCurrencyStringEx3(nDelta.add(nCommisionAndMargin.negate())) + "\r\n";
		return strData;
	}
}

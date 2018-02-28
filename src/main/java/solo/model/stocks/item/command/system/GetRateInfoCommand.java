package solo.model.stocks.item.command.system;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.item.rules.task.manager.TradesBlock;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.telegram.TelegramTransport;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class GetRateInfoCommand extends BaseCommand
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
    	final List<List<String>> aButtons = new LinkedList<List<String>>();
    	List<String> aLine = new LinkedList<String>();
    	for(final RateInfo oRateInfo : aRates)
    	{
			final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(oRateInfo);
			strMessage += getRateData(oRateInfo, oAnalysisResult) + "\r\n";
    		
    		if (aLine.size() == 4)
    		{
    			aButtons.add(aLine);
    			aLine = new LinkedList<String>();
    		}
    		
    		aLine.add(oRateInfo.toString().toUpperCase() + "=" + GetRateChartCommand.NAME + "_" + oRateInfo);
     	}
    	if (aLine.size() > 0)
			aButtons.add(aLine);
   		
		final Map<RateInfo, RateStateShort> oAllRateState = WorkerFactory.getStockSource().getAllRateState();
    	final List<RateInfo> oProspectiveRates = ManagerUtils.getProspectiveRates(oAllRateState, BigDecimal.ZERO);
		for(final RateInfo oExtraDeltaRateInfo : oProspectiveRates)
			addRateButton(oExtraDeltaRateInfo, oAllRateState, aButtons);
		
    	WorkerFactory.getMainWorker().sendSystemMessage(strMessage + 
    			"BUTTONS\r\n" + TelegramTransport.getButtons(aButtons));
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
		
		final TradesBlock oTradesData = WorkerFactory.getStockExchange().getManager().getInfo().getRateLast24Hours().getTotal().getRateTrades().get(oRateInfo);
		
		String strData = MathUtils.toCurrencyStringEx3(nAskPrice) + " / " +   
						MathUtils.toCurrencyStringEx3(nBidPrice) + " / " +  
						MathUtils.toCurrencyStringEx3(nTradePrice) + "[" + strTradeType + "]" +
						(null != oTradesData ? " [" + oTradesData.getPercent() + "%]" : StringUtils.EMPTY) +
						"\r\n";
		strData += "[" + oRateInfo + "] " + MathUtils.toCurrencyStringEx3(nDelta) + " / " + 
					MathUtils.toCurrencyStringEx3(nCommisionAndMargin) + " / " + 
					MathUtils.toCurrencyStringEx3(nDelta.add(nCommisionAndMargin.negate())) + "\r\n";
		return strData;
	}
	
	public static void addRateButton(final RateInfo oRateInfo, final Map<RateInfo, RateStateShort> oAllRateState, List<List<String>> aButtons)
	{
		final RateStateShort oRateStateShort = oAllRateState.get(oRateInfo);
		if (null == oRateStateShort)
			return;
			
		final BigDecimal nBtcVolume = ManagerUtils.convertToBtcVolume(oRateInfo, oRateStateShort.getVolume(), oAllRateState);
		
		final BigDecimal nAskPrice = oRateStateShort.getAskPrice();
		final BigDecimal nDelta = nAskPrice.add(oRateStateShort.getBidPrice().negate());
		final BigDecimal nExtraMagin = ManagerUtils.getExtraMargin(oRateInfo, oRateStateShort);
		final BigDecimal nExtraPercent = MathUtils.getBigDecimal(nExtraMagin.doubleValue() / nAskPrice.doubleValue() * 100, 2);
		
		final TradesBlock oTradesData = WorkerFactory.getStockExchange().getManager().getInfo().getRateLast24Hours().getTotal().getRateTrades().get(oRateInfo);
		
		aButtons.add(Arrays.asList(
						MathUtils.toCurrencyStringEx3(nBtcVolume) + 
						" [" + oRateInfo + "] " +
						MathUtils.toCurrencyStringEx3(nAskPrice) + " / " +   
						MathUtils.toCurrencyStringEx3(nDelta) + " / " +   
						MathUtils.toCurrencyStringEx3(nExtraPercent) + "%" +   
						(null != oTradesData ? " [" + oTradesData.getPercent() + "%]" : StringUtils.EMPTY) + 
						"=/addrate_" + oRateInfo));
	}
}

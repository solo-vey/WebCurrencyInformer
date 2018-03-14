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
		super(strRateInfo, "#type#");
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final String strType = getParameter("#type#").toLowerCase();
		
		final IStockExchange oStockExchange = WorkerFactory.getStockExchange(); 
    	final StateAnalysisResult oStateAnalysisResult = oStockExchange.getLastAnalysisResult();
    	
    	String strMessage = StringUtils.EMPTY;	     	
    	final List<List<String>> aButtons = new LinkedList<List<String>>();
    	List<String> aLine = new LinkedList<String>();
    	for(final RateInfo oRateInfo : WorkerFactory.getStockSource().getRates())
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
    	if (strType.contains("prospectiverates"))
    	{
    		for(final RateInfo oExtraDeltaRateInfo : oProspectiveRates)
    			addRateButton(oExtraDeltaRateInfo, oAllRateState, aButtons);
    	}
    	else
    		if (oProspectiveRates.size() > 0)
    			aButtons.add(Arrays.asList("### Prospective Rates ###=/rate_prospectiverates"));
		
    	WorkerFactory.getMainWorker().sendSystemMessage("HTML_STYLE\r\n" + strMessage + 
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
		
		final BigDecimal nAverageRateProfitabilityPercent = ManagerUtils.getAverageRateProfitabilityPercent(oRateInfo); 
		final boolean bIsLostMoney = (nAverageRateProfitabilityPercent.compareTo(BigDecimal.ZERO) < 0);
		final String strStyle = (bIsLostMoney ? "<code>" : StringUtils.EMPTY);
		final String strCloseStyle = (bIsLostMoney ? "</code>" : StringUtils.EMPTY);
		
		String strData = strStyle + MathUtils.toCurrencyStringEx3(nAskPrice) + " / " +   
						MathUtils.toCurrencyStringEx3(nBidPrice) + " / " +  
						MathUtils.toCurrencyStringEx3(nTradePrice) + "[" + strTradeType + "]" +
						" [" + nAverageRateProfitabilityPercent + "%]" +
						"\r\n";
		
		strData += (bIsLostMoney ? "[" + oRateInfo.toString().toUpperCase() + "] " : "[" + oRateInfo + "] ")  + 
					MathUtils.toCurrencyStringEx3(nDelta) + " / " + 
					MathUtils.toCurrencyStringEx3(nCommisionAndMargin) + " / " + 
					MathUtils.toCurrencyStringEx3(nDelta.add(nCommisionAndMargin.negate())) + "\r\n" + strCloseStyle;
		
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
		final BigDecimal nPercent = ManagerUtils.getAverageRateProfitabilityPercent(oRateInfo);
		
		aButtons.add(Arrays.asList(
						MathUtils.toCurrencyStringEx3(nBtcVolume) + 
						" [" + oRateInfo + "] " +
						MathUtils.toCurrencyStringEx3(nAskPrice) + " / " +   
						MathUtils.toCurrencyStringEx3(nDelta) +   
						" [" + nPercent + "%]" + 
						"=/addrate_" + oRateInfo));
	}
}

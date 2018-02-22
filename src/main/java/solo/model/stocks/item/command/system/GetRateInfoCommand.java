package solo.model.stocks.item.command.system;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.telegram.TelegramTransport;
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
    		
    	final Map<BigDecimal, RateInfo> aExtraDeltaRates = new TreeMap<BigDecimal, RateInfo>();
		final Map<RateInfo, RateStateShort> oAllRateState = WorkerFactory.getStockSource().getAllRateState();
		for(final Entry<RateInfo, RateStateShort> oShortRateInfo : oAllRateState.entrySet())
		{
			if (aRates.contains(oShortRateInfo.getKey()))
				continue;
			
			final BigDecimal nDelta = oShortRateInfo.getValue().getAskPrice().add(oShortRateInfo.getValue().getBidPrice().negate());
			final BigDecimal nCommission = TradeUtils.getCommisionValue(oShortRateInfo.getValue().getAskPrice(), oShortRateInfo.getValue().getBidPrice());
			final BigDecimal nMargin = TradeUtils.getMarginValue(oShortRateInfo.getValue().getAskPrice(), oShortRateInfo.getKey());
			if (nDelta.compareTo(nCommission.add(nMargin)) <= 0)
				continue;
			final BigDecimal nExtraPercent = MathUtils.getBigDecimal(nDelta.add(nCommission.negate()).doubleValue() / oShortRateInfo.getValue().getAskPrice().doubleValue() * 100, 2);
						
			aExtraDeltaRates.put(nExtraPercent.negate(), oShortRateInfo.getKey());
		}
		
		for(final RateInfo oExtraDeltaRateInfo : aExtraDeltaRates.values())
			strMessage += getRateData(oExtraDeltaRateInfo, oAllRateState);

    	if (aLine.size() > 0)
			aButtons.add(aLine);
		
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
		
		String strData = MathUtils.toCurrencyStringEx3(nAskPrice) + " / " +   
						MathUtils.toCurrencyStringEx3(nBidPrice) + " / " +  
						MathUtils.toCurrencyStringEx3(nTradePrice) + "[" + strTradeType + "]\r\n";
		strData += "[" + oRateInfo + "] " + MathUtils.toCurrencyStringEx3(nDelta) + " / " + 
					MathUtils.toCurrencyStringEx3(nCommisionAndMargin) + " / " + 
					MathUtils.toCurrencyStringEx3(nDelta.add(nCommisionAndMargin.negate())) + "\r\n";
		return strData;
	}
	
	public static String getRateData(final RateInfo oRateInfo, final Map<RateInfo, RateStateShort> oAllRateState)
	{
		final RateStateShort oRateStateShort = oAllRateState.get(oRateInfo);
		if (null == oRateStateShort)
			return StringUtils.EMPTY;
		
		final BigDecimal nAskPrice = oRateStateShort.getAskPrice();
		final BigDecimal nBidPrice = oRateStateShort.getBidPrice();
		final BigDecimal nVolume = oRateStateShort.getVolume();
		BigDecimal nBtcVolume = BigDecimal.ZERO;
		
		final RateInfo oToBtcRateInfo = new RateInfo(oRateInfo.getCurrencyFrom(), Currency.BTC);
		if (null != oAllRateState.get(oToBtcRateInfo))
		{
			final BigDecimal nToBtcPrice = oAllRateState.get(oToBtcRateInfo).getBidPrice();
			nBtcVolume = nToBtcPrice.multiply(nVolume);
		}
		else
		{
			final RateInfo oFromBtcRateInfo = RateInfo.getReverseRate(oToBtcRateInfo);
			if (null != oAllRateState.get(oFromBtcRateInfo))
			{
				final BigDecimal nFromBtcPrice = oAllRateState.get(oFromBtcRateInfo).getBidPrice();
				final BigDecimal nToBtcPrice = MathUtils.getBigDecimal(1 / nFromBtcPrice.doubleValue(), TradeUtils.DEFAULT_PRICE_PRECISION);
				nBtcVolume = nToBtcPrice.multiply(nVolume);
			}
			else
			if (oRateInfo.getCurrencyFrom().equals(Currency.BTC))
				nBtcVolume = nVolume;
		}
		
		if (nBtcVolume.compareTo(new BigDecimal(10)) < 0)
			return StringUtils.EMPTY;
		
		final BigDecimal nDelta = nAskPrice.add(nBidPrice.negate());
		final BigDecimal nCommission = TradeUtils.getCommisionValue(nAskPrice, nBidPrice);
		final BigDecimal nExtraPercent = MathUtils.getBigDecimal(nDelta.add(nCommission.negate()).doubleValue() / nAskPrice.doubleValue() * 100, 2);
		
		String strData = "[" + oRateInfo + "]\t\t" + MathUtils.toCurrencyStringEx3(nAskPrice) + " / " +   
						MathUtils.toCurrencyStringEx3(nBidPrice) + " / " +   
						MathUtils.toCurrencyStringEx3(nDelta) + " / " +   
						MathUtils.toCurrencyStringEx3(nExtraPercent) + " / " +   
						MathUtils.toCurrencyStringEx3(nBtcVolume) + "\r\n";
		return strData;
	}
}

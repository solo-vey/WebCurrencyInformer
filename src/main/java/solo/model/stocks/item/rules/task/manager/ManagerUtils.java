package solo.model.stocks.item.rules.task.manager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import solo.model.currency.Currency;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.RulesFactory;
import solo.model.stocks.item.rules.task.trade.ControlerState;
import solo.model.stocks.item.rules.task.trade.ITest;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.TTradeControler;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
import solo.utils.ResourceUtils;

public class ManagerUtils
{
	public static boolean isTestObject(final Object oObject)
	{
		return oObject instanceof ITest;
	}
	
	public static boolean isHasRealControlers(final RateInfo oRateInfo)
	{
		boolean bIsHasRealRule = false;
		for(final IRule oRule : WorkerFactory.getStockExchange().getRules().getRules().values())
		{
			if (!oRule.getRateInfo().equals(oRateInfo))
				continue;
			
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
			bIsHasRealRule |= (null != oControler && !isTestObject(oRule)); 
		}
		
		return bIsHasRealRule;
	}
	
	public static boolean isHasRealWorkingControlers(final RateInfo oRateInfo)
	{
		boolean bIsHasRealWorkingRules = false;
		for(final IRule oRule : WorkerFactory.getStockExchange().getRules().getRules().values())
		{
			if (!oRule.getRateInfo().equals(oRateInfo))
				continue;
			
			if (isTestObject(oRule))
				continue;
			
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
			bIsHasRealWorkingRules |= (null != oControler && ControlerState.WORK.equals(oControler.getControlerState())); 
		}
		
		return bIsHasRealWorkingRules;
	}
	
	public static boolean isHasTestControlers(final RateInfo oRateInfo)
	{
		boolean bIsHasTestRule = false;
		for(final IRule oRule : WorkerFactory.getStockExchange().getRules().getRules().values())
		{
			if (!oRule.getRateInfo().equals(oRateInfo))
				continue;
			
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
			bIsHasTestRule |= (null != oControler && isTestObject(oRule)); 
		}
		
		return bIsHasTestRule;
	}
	
	public static void createTestControler(final RateInfo oRateInfo)
	{
		if (isHasTestControlers(oRateInfo))
			return;
		
		try
		{
			final BigDecimal nSum = TradeUtils.getMinTradeSum(oRateInfo).multiply(new BigDecimal(2));	
			final BigDecimal nMinChangePrice = TradeUtils.getMinChangePrice().multiply(new BigDecimal(2));
			final String strRuleInfo = TTradeControler.NAME + "_" + oRateInfo + "_" + (nSum.compareTo(BigDecimal.ZERO) > 0 ? nSum : nMinChangePrice);
			final IRule oRule = RulesFactory.getRule(strRuleInfo);
			WorkerFactory.getStockExchange().getRules().addRule(oRule);
			
			System.out.printf("Create test trade controler [" + oRateInfo + "]\\r\n");
		}
		catch(final Exception e)
		{
			WorkerFactory.onException("Can't create test trade controler [" + oRateInfo + "]", e);
		}
	}
	
	public static void createTradeControler(RateInfo oRateInfo)
	{
		try
		{
			final BigDecimal nSum = TradeUtils.getMinTradeSum(oRateInfo).multiply(new BigDecimal(2));	
			if (nSum.compareTo(BigDecimal.ZERO) <= 0)
				throw new Exception("Unknown min trade sum for [" + oRateInfo + "]");
			
			final String strRuleInfo = TradeControler.NAME + "_" + oRateInfo + "_" + nSum;
			final IRule oRule = RulesFactory.getRule(strRuleInfo);
			WorkerFactory.getStockExchange().getRules().addRule(oRule);
			
			System.out.printf("Create trade controler [" + oRateInfo + "]\\r\n");
		}
		catch(final Exception e)
		{
			WorkerFactory.onException("Can't create trade controler [" + oRateInfo + "]", e);
		}
	}
	
	public static BigDecimal getMinDayRateVolume()
	{
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strMinDayRateVolume = ResourceUtils.getResource("stock.min_day_rate_volume", oStockExchange.getStockProperties(), "50");
		return MathUtils.fromString(strMinDayRateVolume);
	}
	
	public static List<RateInfo> getProspectiveRates(final Map<RateInfo, RateStateShort> oAllRateState, final BigDecimal nMinExtraPercent)
	{
		final List<RateInfo> aProspectiveRates = new LinkedList<RateInfo>();
		final List<RateInfo> aRates = WorkerFactory.getStockSource().getRates();
		
		final BigDecimal nMinDayRateVolume = ManagerUtils.getMinDayRateVolume();
		for(final Entry<RateInfo, RateStateShort> oShortRateInfo : oAllRateState.entrySet())
		{
			if (aRates.contains(oShortRateInfo.getKey()))
				continue;
			
			final BigDecimal nBtcVolume = ManagerUtils.convertToBtcVolume(oShortRateInfo.getKey(), oShortRateInfo.getValue().getVolume(), oAllRateState);
			if (nBtcVolume.compareTo(nMinDayRateVolume) < 0)
				continue;
		
			aProspectiveRates.add(oShortRateInfo.getKey());
		}
		return aProspectiveRates;
	}
	
	public static BigDecimal convertToBtcVolume(final RateInfo oRateInfo, final BigDecimal nVolume, final Map<RateInfo, RateStateShort> oAllRateState)
	{
		final RateInfo oToBtcRateInfo = new RateInfo(oRateInfo.getCurrencyFrom(), Currency.BTC);
		final RateStateShort oBtcToCurrencyRate = oAllRateState.get(oToBtcRateInfo);
		if (null != oBtcToCurrencyRate)
		{
			final BigDecimal oBtcBidPrice = oBtcToCurrencyRate.getBidPrice();
			return nVolume.multiply(oBtcBidPrice);
		}
		else
		{
			final RateInfo oToBtcRateInfoReverse = RateInfo.getReverseRate(oToBtcRateInfo);
			final RateStateShort oBtcToCurrencyRateReverse = oAllRateState.get(oToBtcRateInfoReverse);
			if (null != oBtcToCurrencyRateReverse)
			{
				final BigDecimal oBtcBidPriceReverse = MathUtils.getBigDecimal(1 / oBtcToCurrencyRateReverse.getBidPrice().doubleValue(), TradeUtils.DEFAULT_PRICE_PRECISION);
				return nVolume.multiply(oBtcBidPriceReverse);
			}			
		}
		
		return BigDecimal.ZERO;
	}
	
	public static BigDecimal getExtraMargin(final RateInfo oRateInfo, final RateStateShort oRateStateShort)
	{
		final BigDecimal nDelta = oRateStateShort.getAskPrice().add(oRateStateShort.getBidPrice().negate());
		final BigDecimal nCommission = TradeUtils.getCommisionValue(oRateStateShort.getAskPrice(), oRateStateShort.getBidPrice());
		final BigDecimal nMargin = TradeUtils.getMarginValue(oRateStateShort.getAskPrice(), oRateInfo);
		final BigDecimal nCommissionAnMargin = nCommission.add(nMargin);
		return nDelta.add(nCommissionAnMargin.negate());
	}
	
	public static BigDecimal getAverageRateProfitabilityPercent(final RateInfo oRateInfo, final int nHoursCount)
	{
		final StockManagesInfo oStockManagesInfo = WorkerFactory.getStockExchange().getManager().getInfo();
		final Map<Integer, RateTradesBlock> oRatePriodTrades = oStockManagesInfo.getRateLast24Hours().getPeriods();
		
		final int nStartHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		BigDecimal nTotalPercent = BigDecimal.ZERO;
		for(int nPos = 1; nPos <= nHoursCount; nPos++)
		{
			final int nHour = (nStartHour - nPos >= 0 ? nStartHour - nPos : (nStartHour - nPos) + 24);
			final RateTradesBlock oHourRateTradesBlock = oRatePriodTrades.get(nHour);
			if (null == oHourRateTradesBlock)
				continue;
			final TradesBlock oRateTradesBlock = oHourRateTradesBlock.getRateTrades().get(oRateInfo);
			if (null == oRateTradesBlock)
				continue;
			final BigDecimal nPercent = oRateTradesBlock.getPercent();
			nTotalPercent = nTotalPercent.add(nPercent);
		}
		
		return MathUtils.getBigDecimal(nTotalPercent.doubleValue() / nHoursCount, 3);
	}
	
	public static BigDecimal getMinRateHourProfitabilityPercent(final RateInfo oRateInfo, final int nHoursCount)
	{
		final StockManagesInfo oStockManagesInfo = WorkerFactory.getStockExchange().getManager().getInfo();
		final Map<Integer, RateTradesBlock> oRatePriodTrades = oStockManagesInfo.getRateLast24Hours().getPeriods();
		
		final int nStartHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		BigDecimal nMinPercent = new BigDecimal(Integer.MAX_VALUE);
		for(int nPos = 1; nPos <= nHoursCount; nPos++)
		{
			final int nHour = (nStartHour - nPos >= 0 ? nStartHour - nPos : (nStartHour - nPos) + 24);
			final RateTradesBlock oHourRateTradesBlock = oRatePriodTrades.get(nHour);
			if (null == oHourRateTradesBlock)
				continue;
			
			final TradesBlock oRateTradesBlock = oHourRateTradesBlock.getRateTrades().get(oRateInfo);
			if (null == oRateTradesBlock)
				continue;
			
			final BigDecimal nPercent = oRateTradesBlock.getPercent();
			if (nPercent.compareTo(nMinPercent) < 0)
				nMinPercent = nPercent;
		}
		
		return (nMinPercent.equals(new BigDecimal(Integer.MAX_VALUE)) ? BigDecimal.ZERO : nMinPercent);
	}
}


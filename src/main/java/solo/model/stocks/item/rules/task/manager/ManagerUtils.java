package solo.model.stocks.item.rules.task.manager;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.RulesFactory;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.rules.task.trade.ITest;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.TTradeControler;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.item.rules.task.trade.TradesInfo;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
import solo.utils.ResourceUtils;

public class ManagerUtils
{
	public static boolean isTestObject(final Object oObject)
	{
		return oObject instanceof ITest;
	}
	
	public static boolean isHasRealRules(final RateInfo oRateInfo)
	{
		boolean bIsHasRealRule = false;
		for(final IRule oRule : WorkerFactory.getStockExchange().getRules().getRules().values())
		{
			if (!oRule.getRateInfo().equals(oRateInfo))
				continue;
			
			bIsHasRealRule |= (!isTestObject(oRule)); 
		}
		
		return bIsHasRealRule;
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
			bIsHasRealWorkingRules |= (null != oControler && (oControler.getControlerState().isWork() || oControler.getControlerState().isWait())); 
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
			final BigDecimal nSum = TradeUtils.getMinTradeSum(oRateInfo).multiply(new BigDecimal(2.2));	
			final BigDecimal nMinChangePrice = TradeUtils.getMinChangePrice().multiply(new BigDecimal(2.2));
			final String strRuleInfo = TTradeControler.NAME + "_" + oRateInfo + "_" + (nSum.compareTo(BigDecimal.ZERO) > 0 ? nSum : nMinChangePrice);
			final IRule oRule = RulesFactory.getRule(strRuleInfo);
			WorkerFactory.getStockExchange().getRules().addRule(oRule);
			
			System.out.printf("Create test trade controler [" + oRateInfo + "]\r\n");
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
			final BigDecimal nSum = TradeUtils.getRoundedPrice(oRateInfo, TradeUtils.getMinTradeSum(oRateInfo).multiply(new BigDecimal(2.2)));	
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

	public static BigDecimal get24HoursRateProfitabilityPercent(final RateInfo oRateInfo)
	{
		final int nBackViewProfitabilityHours = ResourceUtils.getIntFromResource("stock.back_view.profitability.hours", WorkerFactory.getStockExchange().getStockProperties(), 3);
		return getAverageRateProfitabilityPercent(oRateInfo, nBackViewProfitabilityHours);
	}
	
	public static BigDecimal getAverageRateProfitabilityPercent(final RateInfo oRateInfo)
	{
		final int nBackViewProfitabilityHours = ResourceUtils.getIntFromResource("stock.back_view.profitability.hours", WorkerFactory.getStockExchange().getStockProperties(), 3);
		return getAverageRateProfitabilityPercent(oRateInfo, nBackViewProfitabilityHours);
	}
	
	public static List<Entry<Integer, RateTradesBlock>> getHoursTrades(final int nHoursCount)
	{
		final StockManagesInfo oStockManagesInfo = WorkerFactory.getStockExchange().getManager().getInfo();
		final Map<Integer, RateTradesBlock> oRatePriodTrades = oStockManagesInfo.getRateLast24Hours().getPeriods();
		
		final int nStartHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		final List<Entry<Integer, RateTradesBlock>> aResult = new LinkedList<Entry<Integer, RateTradesBlock>>();
		for(int nPos = 1; nPos <= nHoursCount; nPos++)
		{
			final int nHour = (nStartHour - nPos >= 0 ? nStartHour - nPos : (nStartHour - nPos) + 24);
			final RateTradesBlock oHourRateTradesBlock = oRatePriodTrades.get(nHour);
			if (null != oHourRateTradesBlock)
				aResult.add(new AbstractMap.SimpleEntry<Integer, RateTradesBlock>(nHour, oHourRateTradesBlock));
		}
		
		return aResult;
	}
	
	public static Map<RateInfo, List<Entry<Integer, TradesBlock>>> convertFromHoursTradesToRateTrades(final List<Entry<Integer, RateTradesBlock>> aHoursTrades)
	{
		final Map<RateInfo, List<Entry<Integer, TradesBlock>>> aRates = new HashMap<RateInfo, List<Entry<Integer, TradesBlock>>>();
		for(final Entry<Integer, RateTradesBlock> oTradeInfo : aHoursTrades)
		{
			final RateTradesBlock oHourRateTradesBlock = oTradeInfo.getValue();
			for(final Entry<RateInfo, TradesBlock> oRateTradeBlock : oHourRateTradesBlock.getRateTrades().entrySet())
			{	
				if (null == aRates.get(oRateTradeBlock.getKey()))
					aRates.put(oRateTradeBlock.getKey(), new LinkedList<Entry<Integer, TradesBlock>>());
				aRates.get(oRateTradeBlock.getKey()).add(new AbstractMap.SimpleEntry<Integer, TradesBlock>(oTradeInfo.getKey(), oRateTradeBlock.getValue()));
			}
		}
		
		return aRates;
	}
	
	public static BigDecimal getAverageRateProfitabilityPercent(final RateInfo oRateInfo, final int nHoursCount)
	{
		try
		{
			BigDecimal nTotalPercent = BigDecimal.ZERO;
			final List<Entry<Integer, RateTradesBlock>> aHoursTrades = getHoursTrades(nHoursCount);
			for(final Entry<Integer, RateTradesBlock> oTradeInfo : aHoursTrades)
			{
				final RateTradesBlock oHourRateTradesBlock = oTradeInfo.getValue();
				final TradesBlock oRateTradesBlock = oHourRateTradesBlock.getRateTrades().get(oRateInfo);
				if (null == oRateTradesBlock)
					continue;
				
				final BigDecimal nPercent = oRateTradesBlock.getPercent();
				nTotalPercent = nTotalPercent.add(nPercent);
			}
			
			return MathUtils.getBigDecimal(nTotalPercent.doubleValue() / nHoursCount, 3);
		}
		catch(final Exception e)
		{
			WorkerFactory.onException("getAverageRateProfitabilityPercent", e);
			return BigDecimal.ZERO;
		}
	}
	
	public static BigDecimal getMinRateHourProfitabilityPercent(final RateInfo oRateInfo)
	{
		final int nBackViewProfitabilityHours = ResourceUtils.getIntFromResource("stock.back_view.profitability.hours", WorkerFactory.getStockExchange().getStockProperties(), 3);
		return getMinRateHourProfitabilityPercent(oRateInfo, nBackViewProfitabilityHours);
	}
	
	public static BigDecimal getMinRateHourProfitabilityPercent(final RateInfo oRateInfo, final int nHoursCount)
	{
		BigDecimal nMinPercent = new BigDecimal(Integer.MAX_VALUE);
		final List<Entry<Integer, RateTradesBlock>> aHoursTrades = getHoursTrades(nHoursCount);
		for(final Entry<Integer, RateTradesBlock> oTradeInfo : aHoursTrades)
		{
			final RateTradesBlock oHourRateTradesBlock = oTradeInfo.getValue();
			final TradesBlock oRateTradesBlock = oHourRateTradesBlock.getRateTrades().get(oRateInfo);
			if (null == oRateTradesBlock)
				continue;
			
			final BigDecimal nPercent = oRateTradesBlock.getPercent();
			if (nPercent.compareTo(nMinPercent) < 0)
				nMinPercent = nPercent;
		}
		
		return (nMinPercent.equals(new BigDecimal(Integer.MAX_VALUE)) ? BigDecimal.ZERO : nMinPercent);
	}
	
	public static Map<Currency, CurrencyAmount> calculateStockMoney(final StockUserInfo oUserInfo, final Rules oRules)
	{
		final Map<Currency, BigDecimal> aLocked = new HashMap<Currency, BigDecimal>();
		
		for(final IRule oRule : oRules.getRules().values())
		{
			final ITradeControler oTradeControler = TradeUtils.getRuleAsTradeControler(oRule);
			if (null == oTradeControler || ManagerUtils.isTestObject(oTradeControler) || oTradeControler.getControlerState().isStopped())
				continue;
			
			final TradesInfo oTradesInfo = oTradeControler.getTradesInfo();
			final RateInfo oRateInfo = oTradesInfo.getRateInfo();			
			
			final BigDecimal nControlerFreeSum = oTradesInfo.getFreeSum();
			final BigDecimal nLockedSumCurrencyTo = (aLocked.containsKey(oRateInfo.getCurrencyTo()) ? aLocked.get(oRateInfo.getCurrencyTo()) : BigDecimal.ZERO);
			aLocked.put(oRateInfo.getCurrencyTo(), nLockedSumCurrencyTo.add(nControlerFreeSum));				
			
			final BigDecimal nControlerFreeVolume = oTradesInfo.getFreeVolume();
			final BigDecimal nLockedVolumeCurrencyFrom = (aLocked.containsKey(oRateInfo.getCurrencyFrom()) ? aLocked.get(oRateInfo.getCurrencyFrom()) : BigDecimal.ZERO);
			aLocked.put(oRateInfo.getCurrencyFrom(), nLockedVolumeCurrencyFrom.add(nControlerFreeVolume));	
		}
	
		final Map<Currency, CurrencyAmount> oMoney = new HashMap<Currency, CurrencyAmount>();
		for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oUserInfo.getMoney().entrySet())
		{
			final BigDecimal nLocked = (aLocked.containsKey(oCurrencyInfo.getKey()) ? aLocked.get(oCurrencyInfo.getKey()) : BigDecimal.ZERO);
			final CurrencyAmount oRealCurrencyAmount = new CurrencyAmount(oCurrencyInfo.getValue().getBalance(), nLocked.add(oCurrencyInfo.getValue().getLocked()));
			oMoney.put(oCurrencyInfo.getKey(), oRealCurrencyAmount);
		}
	
		return oMoney;
	}
}


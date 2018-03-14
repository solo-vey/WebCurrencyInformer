package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.ResourceUtils;

public class StockManagesInfo extends BaseObject implements Serializable
{
	private static final long serialVersionUID = -7601846839784506296L;
	
	protected CurrencyTradesBlock m_oTotal = new CurrencyTradesBlock();
	protected PeriodTradesBlock m_oMonthsTotal = new PeriodTradesBlock();
	protected PeriodTradesBlock m_oDaysTotal = new PeriodTradesBlock();
	protected PeriodTradesBlock m_oHoursTotal = new PeriodTradesBlock();
	protected CycleTradesBlock m_oLast24Hours = new CycleTradesBlock();
	protected RateCycleTradesBlock m_oRateLast24Hours = new RateCycleTradesBlock();

	public StockManagesInfo()
	{
	}
	
	public CurrencyTradesBlock getTotal()
	{
		return m_oTotal;
	}
	
	public PeriodTradesBlock getMonthsTotal()
	{
		return m_oMonthsTotal;
	}
	
	public PeriodTradesBlock getDaysTotal()
	{
		return m_oDaysTotal;
	}	
	
	public PeriodTradesBlock getHoursTotal()
	{
		return m_oHoursTotal;
	}
	
	public CycleTradesBlock getLast24Hours()
	{
		if (null == m_oLast24Hours)
			m_oLast24Hours = new CycleTradesBlock();
		return m_oLast24Hours;
	}
	
	public RateCycleTradesBlock getRateLast24Hours()
	{
		if (null == m_oRateLast24Hours)
			m_oRateLast24Hours = new RateCycleTradesBlock();
		return m_oRateLast24Hours;
	}
	
	public void tradeStart(final TaskTrade oTaskTrade) 
	{
		
	}
	
	public void tradeDone(final TaskTrade oTaskTrade) 
	{
		final Calendar oCalendar = Calendar.getInstance();
		if (!ManagerUtils.isTestObject(oTaskTrade))
		{		
			getTotal().addTrade(oTaskTrade);	
			getMonthsTotal().addTrade(oCalendar.get(Calendar.MONTH) + 1, oTaskTrade);
			getDaysTotal().addTrade(oCalendar.get(Calendar.DAY_OF_MONTH), oTaskTrade);
			getHoursTotal().addTrade(oCalendar.get(Calendar.HOUR_OF_DAY), oTaskTrade);
		}
		
		if (!ManagerUtils.isTestObject(oTaskTrade) || !ManagerUtils.isHasRealWorkingControlers(oTaskTrade.getRateInfo()))
		{
			getLast24Hours().addTrade(oCalendar.get(Calendar.HOUR_OF_DAY), oTaskTrade);
			getRateLast24Hours().addTrade(oCalendar.get(Calendar.HOUR_OF_DAY), oTaskTrade);
		}
	}
	
	public void buyDone(final TaskTrade oTaskTrade) 
	{
		
	}
	
	public void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume) 
	{
		
	}
	
	public void addSell(final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) 
	{
		
	} 
	
	public String asString(final String strType)
	{
		final Calendar oCalendar = Calendar.getInstance();
		if (strType.equalsIgnoreCase("TOTAL") || StringUtils.isBlank(strType))
			return getTotal().toString();
		
		if (strType.equalsIgnoreCase("LAST24HOURS"))
			return getLast24Hours().toString();
		
		if (strType.equalsIgnoreCase("RATELAST24HOURS"))
			return getRateLast24Hours().toString();
		
		if (strType.equalsIgnoreCase("RATELAST24FORHOURS"))
		{
			String strMessage = StringUtils.EMPTY;
			for(final Entry<Integer, RateTradesBlock> oHourTradesInfo  : getRateLast24Hours().getPeriods().entrySet())
				strMessage += oHourTradesInfo.getKey() + "\r\n" + oHourTradesInfo.getValue() + "\r\n";
			return strMessage;
		}
		
		if (strType.equalsIgnoreCase("RATELASTHOURS"))
		{
			String strMessage = StringUtils.EMPTY;
			final int nHoursCount = ResourceUtils.getIntFromResource("stock.back_view.profitability.hours", WorkerFactory.getStockExchange().getStockProperties(), 3);
			final List<Entry<Integer, RateTradesBlock>> aHoursTrades = ManagerUtils.getHoursTrades(nHoursCount);
			final Map<RateInfo, List<Entry<Integer, TradesBlock>>> aRates = ManagerUtils.convertFromHoursTradesToRateTrades(aHoursTrades);
			
			final TreeMap<BigDecimal, RateInfo> aSorted = new TreeMap<BigDecimal, RateInfo>();
			final Map<RateInfo, TradesBlock> oRateTotals = getRateLast24Hours().getTotal().getRateTrades();
			for(final Entry<RateInfo, TradesBlock> oTradesInfo : oRateTotals.entrySet())
			{
				if (!oTradesInfo.getKey().getIsReverse())
					aSorted.put(oTradesInfo.getValue().getPercent(), oTradesInfo.getKey());
			}
			
			for(final RateInfo oRateInfo : aSorted.values())
			{
				String strRateMessage = oRateInfo + " : ";
				strRateMessage += "24h " + oRateTotals.get(oRateInfo).asString("only_percent") + ", ";
				final List<Entry<Integer, TradesBlock>> oRateTrades = aRates.get(oRateInfo);
				if (null != oRateTrades)
				{
					for(final Entry<Integer, TradesBlock> oHourTrades : oRateTrades)
						strRateMessage += oHourTrades.getKey() + oHourTrades.getValue().asString("only_percent") + ", ";
				}
				
				final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo);
				strRateMessage += "\r\n                ";
				strRateMessage += "24h" + oRateTotals.get(oReverseRateInfo).asString("only_percent") + ", ";
				final List<Entry<Integer, TradesBlock>> oReverseRateTrades = aRates.get(oReverseRateInfo);
				if (null != oReverseRateTrades)
				{
					for(final Entry<Integer, TradesBlock> oHourTrades : oReverseRateTrades)
						strRateMessage += oHourTrades.getKey() + oHourTrades.getValue().asString("only_percent") + ", ";
				}
				strMessage = strRateMessage + "\r\n" + strMessage; 
			}
			return strMessage;
		}
		
		if (strType.equalsIgnoreCase("HOURS"))
			return getHoursTotal().toString();
		
		if (strType.equalsIgnoreCase("HOUR"))
		{
			final Map<Integer, CurrencyTradesBlock> oHours = getHoursTotal().getPeriods();
			final int nHour = oCalendar.get(Calendar.HOUR_OF_DAY);
			return (null != oHours.get(nHour) ? oHours.get(nHour).toString() : "No data");
		}
		
		if (strType.equalsIgnoreCase("DAYS"))
			return getDaysTotal().toString();
		
		if (strType.equalsIgnoreCase("DAY"))
		{
			final Map<Integer, CurrencyTradesBlock> oDays = getDaysTotal().getPeriods();
			final int nDay = oCalendar.get(Calendar.DAY_OF_MONTH);
			return (null != oDays.get(nDay) ? oDays.get(nDay).toString() : "No data");
		}
		
		if (strType.equalsIgnoreCase("MONTHS"))
			return getMonthsTotal().toString();
		
		if (strType.equalsIgnoreCase("MONTH"))
			return getMonthsTotal().getPeriods().get(oCalendar.get(Calendar.MONTH) + 1).toString();
		
		return "Unknown type [" + strType + "] of info [TOTAL, HOURS, HOUR, DAYS, DAY, MONTHS, MONTH, LAST24HOURS, RATELAST24HOURS, RATELAST24FORHOURS]";
	}
}

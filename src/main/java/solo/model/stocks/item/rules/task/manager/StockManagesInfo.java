package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import org.apache.commons.lang.StringUtils;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.rules.task.trade.ITest;
import solo.model.stocks.item.rules.task.trade.TaskTrade;

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
	
	protected CurrencyTradesBlock getTotal()
	{
		return m_oTotal;
	}
	
	protected PeriodTradesBlock getMonthsTotal()
	{
		return m_oMonthsTotal;
	}
	
	protected PeriodTradesBlock getDaysTotal()
	{
		return m_oDaysTotal;
	}	
	
	protected PeriodTradesBlock getHoursTotal()
	{
		return m_oHoursTotal;
	}
	
	protected CycleTradesBlock getLast24Hours()
	{
		if (null == m_oLast24Hours)
			m_oLast24Hours = new CycleTradesBlock();
		return m_oLast24Hours;
	}
	
	protected RateCycleTradesBlock getRateLast24Hours()
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
		
		if (!ManagerUtils.isTestObject(oTaskTrade) || !ManagerUtils.isHasRealRules(oTaskTrade.getRateInfo()))
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
		
		if (strType.equalsIgnoreCase("HOURS"))
			return getHoursTotal().toString();
		
		if (strType.equalsIgnoreCase("HOUR"))
			return getHoursTotal().getPeriods().get(oCalendar.get(Calendar.HOUR_OF_DAY)).toString();
		
		if (strType.equalsIgnoreCase("DAYS"))
			return getDaysTotal().toString();
		
		if (strType.equalsIgnoreCase("DAY"))
			return getDaysTotal().getPeriods().get(oCalendar.get(Calendar.DAY_OF_MONTH)).toString();
		
		if (strType.equalsIgnoreCase("MONTHS"))
			return getMonthsTotal().toString();
		
		if (strType.equalsIgnoreCase("MONTH"))
			return getMonthsTotal().getPeriods().get(oCalendar.get(Calendar.MONTH) + 1).toString();
		
		return "Unknown type [" + strType + "] of info [TOTAL, HOURS, HOUR, DAYS, DAY, MONTHS, MONTH, LAST24HOURS, RATELAST24HOURS]";
	}
}

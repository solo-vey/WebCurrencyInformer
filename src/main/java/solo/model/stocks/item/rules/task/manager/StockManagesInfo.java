package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.rules.task.trade.TaskTrade;

public class StockManagesInfo extends BaseObject implements Serializable
{
	private static final long serialVersionUID = -7601846839784506296L;
	
	public CurrencyTradesBlock m_oTotal = new CurrencyTradesBlock();
	public PeriodTradesBlock m_oMonthsTotal = new PeriodTradesBlock();
	public PeriodTradesBlock m_oDaysTotal = new PeriodTradesBlock();
	public PeriodTradesBlock m_oHoursTotal = new PeriodTradesBlock();

	public StockManagesInfo()
	{
	}
	
	
	public void tradeStart(final TaskTrade oTaskTrade) 
	{
		
	}
	
	public void tradeDone(final TaskTrade oTaskTrade) 
	{
		m_oTotal.addTrade(oTaskTrade);
		
		final Calendar oCalendar = Calendar.getInstance();
		m_oMonthsTotal.addTrade(oCalendar.get(Calendar.MONTH + 1), oTaskTrade);
		m_oDaysTotal.addTrade(oCalendar.get(Calendar.DAY_OF_MONTH), oTaskTrade);
		m_oHoursTotal.addTrade(oCalendar.get(Calendar.HOUR_OF_DAY), oTaskTrade);
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
		if (strType.equalsIgnoreCase("TOTAL") || StringUtils.isBlank(strType))
			return m_oTotal.toString();
		
		if (strType.equalsIgnoreCase("HOURS"))
			return m_oHoursTotal.toString();
		
		if (strType.equalsIgnoreCase("DAYS"))
			return m_oDaysTotal.toString();
		
		if (strType.equalsIgnoreCase("MONTHS"))
			return m_oMonthsTotal.toString();
		
		return "Unknown type [" + strType + "] of info [TOTAL, HOURS, DAYS, MONTHS]";
	}
}

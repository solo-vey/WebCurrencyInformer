package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.RateInfo;
import solo.transport.MessageLevel;
import solo.utils.MathUtils;

public class TradeControlerWait extends TradeControler
{
	private static final long serialVersionUID = 2548242566461334806L;

	final static public String TRADE_WAIT = "#wait#";
	
	public Date m_oCreateAfterDate;
	public int m_nMinutes = 1;
	
	public TradeControlerWait(RateInfo oRateInfo, String strCommandLine)
	{
		super(oRateInfo, strCommandLine, TRADE_WAIT);
		if (StringUtils.isNotBlank(getParameter(TRADE_WAIT)))
			m_nMinutes = getParameterAsInt(TRADE_WAIT);
		m_oCreateAfterDate = new Date();
	}
	
	@Override public String getType()
	{
		return "CONTROLERWAIT";   
	}
	
	@Override protected void checkTrade(final TaskTrade oTaskTrade, boolean bIsBuyPrecent, List<TaskTrade> aTaskTrades)
	{
		if (bIsBuyPrecent || aTaskTrades.size() < m_nMaxTrades)
			return;
		
		final Calendar oCalendar = Calendar.getInstance();
	    oCalendar.setTime(new Date());
	    oCalendar.add(Calendar.MINUTE, -15);
	    final Date oMaxDateCreate = oCalendar.getTime();			

	    if (null != oTaskTrade.getTradeInfo().getOrder().getCreated() && oTaskTrade.getTradeInfo().getOrder().getCreated().before(oMaxDateCreate))
	    {
	    	final BigDecimal nCriticalPrice = oTaskTrade.getTradeInfo().getCriticalPrice();
	    	final BigDecimal nNewCriticalPrice = MathUtils.getBigDecimal(nCriticalPrice.doubleValue() * RESET_CRITICAL_PRICE_PERCENT, TradeUtils.getPricePrecision(m_oRateInfo));
	    	oTaskTrade.getTradeInfo().setCriticalPrice(nNewCriticalPrice);
	    	sendMessage(getType() + "\r\n" + oTaskTrade.getInfo(null) + "\r\n" +
	    			"Reset critical price " + MathUtils.toCurrencyString(nNewCriticalPrice)); 
	    }
	}
	
	protected void createNewTrade()
	{
		if (null == m_oCreateAfterDate)
			setNewCreateAfter();
		
		if (null != m_oCreateAfterDate && m_oCreateAfterDate.after(new Date()))
			return;
		
		super.createNewTrade();
		m_oCreateAfterDate = null;
	}

	public void buyDone(final TaskTrade oTaskTrade) 
	{
		setNewCreateAfter();			
	}

	public void setNewCreateAfter() 
	{
		final Calendar oCalendar = Calendar.getInstance();
	    oCalendar.setTime(new Date());
	    oCalendar.add(Calendar.MINUTE, m_nMinutes);
	    m_oCreateAfterDate = oCalendar.getTime();			
	}
}

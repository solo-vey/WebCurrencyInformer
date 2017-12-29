package solo.model.stocks.item.rules.task.trade;

import java.util.Calendar;
import java.util.Date;

import solo.model.stocks.item.RateInfo;

public class TradeControlerWait extends TradeControler
{
	private static final long serialVersionUID = 2548242566461334806L;

	final static public String TRADE_WAIT = "#wait#";
	
	public Date m_oCreateAfterDate = new Date();
	public int m_nMinutes = 1;
	
	public TradeControlerWait(RateInfo oRateInfo, String strCommandLine)
	{
		super(oRateInfo, strCommandLine, TRADE_WAIT);
		m_nMinutes = getParameterAsInt(TRADE_WAIT);
	}
	
	@Override public String getType()
	{
		return "CONTROLERWAIT";   
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

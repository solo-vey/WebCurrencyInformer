package solo.model.stocks.item.utils;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

public class PeriodTracker
{
	final protected int m_nMinutCount;
	protected Date m_oDateNextTriggering = new Date();
	
	public PeriodTracker(final int nMinutCount)
	{
		m_nMinutCount = nMinutCount;
		setNewDateTriggering();
	}

	void setNewDateTriggering()
	{
		m_oDateNextTriggering =  DateUtils.addMinutes(new Date(), m_nMinutCount);
	}
	
	public boolean getIsTrigerred()
	{
		if (m_oDateNextTriggering.after(new Date()))
			return false;
		
		setNewDateTriggering();
		return true;			
	}
}

package solo.transport;

public enum MessageLevel
{
	ALL(0),
	TRACE(10),
	DEBUG(100),
	TESTTRADERESULT(900),
	TRADERESULT(1000),
	ERROR(2000);
	
	protected int m_nLevel;
	
	MessageLevel(final int nLevel)
	{
		m_nLevel = nLevel;
	}
	
	public int getLevel()
	{
		return m_nLevel;
	}
	
	public boolean isLevelHigh(final MessageLevel oMessageLevel)
	{
		return oMessageLevel.getLevel() <= getLevel();
	}
}

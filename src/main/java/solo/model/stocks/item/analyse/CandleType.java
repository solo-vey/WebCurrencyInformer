package solo.model.stocks.item.analyse;

public enum CandleType
{
	NONE("-"),
	FALL("v"),
	GROW("^");
	
	final protected String m_strValue;
	
	CandleType(final String strValue)
	{
		m_strValue = strValue;
	}
	
	@Override public String toString()
	{
		return m_strValue;
	}
}

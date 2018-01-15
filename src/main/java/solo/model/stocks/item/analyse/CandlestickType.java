package solo.model.stocks.item.analyse;

public enum CandlestickType
{
	UNKNOWN(TrendType.CALM),
	GROWTH(TrendType.GROWTH),
	FALL(TrendType.FALL),
	START_GROWTH(TrendType.GROWTH),
	START_FALL(TrendType.FALL),
	
	THREE_WHITE(TrendType.GROWTH),
	THREE_BLACK(TrendType.FALL),
	MORNING_STAR(TrendType.GROWTH),
	EVENING_STAR(TrendType.FALL),
	BLACK_AND_TWO_WHITE(TrendType.GROWTH),
	WHITE_AND_TWO_BLACK(TrendType.FALL),

	BLACK_TO_WHITE(TrendType.GROWTH),
	WHITE_TO_BLACK(TrendType.FALL),
	
	CALM(TrendType.CALM);
	
	final protected TrendType m_oTrendType;
	
	CandlestickType(final TrendType oTrendType)
	{
		m_oTrendType = oTrendType;
	}
	
	public Boolean isGrowth()
	{
		return m_oTrendType.isGrowth();
	}
	
	public Boolean isFall()
	{
		return m_oTrendType.isFall();
	}

	public Boolean isCalm()
	{
		return m_oTrendType.isCalm();
	}
}

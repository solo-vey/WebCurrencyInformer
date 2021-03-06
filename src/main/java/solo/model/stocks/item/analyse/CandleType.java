package solo.model.stocks.item.analyse;

public enum CandleType
{
	NONE(TrendType.CALM, CandleGroupType.NONE),
	DOJI_GROWTH(TrendType.CALM, CandleGroupType.DOJI),
	DOJI_FALL(TrendType.CALM, CandleGroupType.DOJI),
	MARUBOZU_GROWTH(TrendType.GROWTH, CandleGroupType.STANDARD),
	MARUBOZU_FALL(TrendType.FALL, CandleGroupType.STANDARD),
	HAMMER_GROWTH(TrendType.CALM, CandleGroupType.HAMMER),
	HAMMER_FALL(TrendType.CALM, CandleGroupType.HAMMER),
	HANGING_MAN_GROWTH(TrendType.CALM, CandleGroupType.HAMMER),
	HANGING_MAN_FALL(TrendType.CALM, CandleGroupType.HAMMER),
	LONG_SHADOW_GROWTH(TrendType.GROWTH, CandleGroupType.STANDARD),
	LONG_SHADOW_FALL(TrendType.FALL, CandleGroupType.STANDARD),
	LONG_BODY_GROWTH(TrendType.GROWTH, CandleGroupType.STANDARD),
	LONG_BODY_FALL(TrendType.FALL, CandleGroupType.STANDARD);
	
	protected final TrendType m_oTrendType;
	protected final CandleGroupType m_oGroupType;
	
	CandleType(final TrendType oTrendType, final CandleGroupType oGroupType)
	{
		m_oTrendType = oTrendType;
		m_oGroupType = oGroupType;
	}
	
	public TrendType getTrendType()
	{
		return m_oTrendType;
	}
	
	public CandleGroupType getGroupType()
	{
		return m_oGroupType;
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
	
	public Boolean isGrowth(final CandleGroupType oGroupType)
	{
		return m_oTrendType.isGrowth() && m_oGroupType.equals(oGroupType);
	}
	
	public Boolean isFall(final CandleGroupType oGroupType)
	{
		return m_oTrendType.isFall() && m_oGroupType.equals(oGroupType);
	}
}

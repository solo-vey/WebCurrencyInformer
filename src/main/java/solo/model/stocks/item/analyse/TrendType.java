package solo.model.stocks.item.analyse;

public enum TrendType
{
	/** Спокойствие */
	CALM,
	/** Рост */
	GROWTH,
	/** Быстрый рост */
	FAST_GROWTH,
	/** Падение */
	FALL,
	/** Быстрое падение */
	FAST_FALL;
	
	public Boolean isGrowth()
	{
		return this.equals(TrendType.GROWTH) || this.equals(TrendType.FAST_GROWTH);
	}
	
	public Boolean isFall()
	{
		return this.equals(TrendType.FALL) || this.equals(TrendType.FAST_FALL);
	}
	
	public Boolean isCalm()
	{
		return this.equals(TrendType.CALM);
	}
}

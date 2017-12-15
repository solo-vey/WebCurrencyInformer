package solo.model.stocks.oracle;

import java.math.BigDecimal;

import solo.CurrencyInformer;
import solo.model.stocks.BaseObject;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.TrendType;
import solo.utils.MathUtils;

public class RateForecast extends BaseObject
{
	final protected RateInfo m_oRateInfo;
	final protected BigDecimal m_oPrice;
	final protected TrendType m_oTrendType;
	
	public RateForecast(final RateInfo oRateInfo, final double nPrice, final TrendType oTrendType)
	{
		m_oRateInfo = oRateInfo;
		m_oPrice = MathUtils.getBigDecimal(nPrice, CurrencyInformer.DECIMAL_SCALE);
		m_oTrendType = oTrendType;
	}
	
	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}
	
	public BigDecimal getPrice()
	{
		return m_oPrice;
	}
	
	public TrendType getTrendType()
	{
		return m_oTrendType;
	}
}

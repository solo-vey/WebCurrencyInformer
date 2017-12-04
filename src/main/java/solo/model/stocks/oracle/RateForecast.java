package solo.model.stocks.oracle;

import java.math.BigDecimal;

import solo.CurrencyInformer;
import solo.model.stocks.BaseObject;
import solo.model.stocks.item.RateInfo;
import solo.utils.MathUtils;

public class RateForecast extends BaseObject
{
	final protected RateInfo m_oRateInfo;
	final protected BigDecimal m_oPrice;
	
	public RateForecast(final RateInfo oRateInfo, final double nPrice)
	{
		m_oRateInfo = oRateInfo;
		m_oPrice = MathUtils.getBigDecimal(nPrice, CurrencyInformer.DECIMAL_SCALE);
	}
	
	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}
	
	public BigDecimal getPrice()
	{
		return m_oPrice;
	}
}
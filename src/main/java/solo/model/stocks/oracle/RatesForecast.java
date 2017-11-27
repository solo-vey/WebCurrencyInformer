package solo.model.stocks.oracle;

import java.util.HashMap;
import java.util.Map;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.RateInfo;

public class RatesForecast extends BaseObject
{
	final protected Map<RateInfo, RateForecast> m_oRateForecast = new HashMap<RateInfo, RateForecast>();

	public void addForecust(final RateForecast oRateForecast) 
	{
		m_oRateForecast.put(oRateForecast.getRateInfo(), oRateForecast);
	}

	public RateForecast getForecust(final RateInfo oRateInfo) 
	{
		return m_oRateForecast.get(oRateInfo);
	}
}

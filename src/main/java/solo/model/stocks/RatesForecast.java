package solo.model.stocks;

import java.util.HashMap;
import java.util.Map;

public class RatesForecast extends BaseObject
{
	final protected Map<RateInfo, RateForecast> m_oRateForecast = new HashMap<RateInfo, RateForecast>();

	public void addForecust(final RateForecast oRateForecast) 
	{
		m_oRateForecast.put(oRateForecast.getRateInfo(), oRateForecast);
	}
}

package solo.model.stocks.item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import solo.model.stocks.BaseObject;

public class StockRateStates extends BaseObject
{
	private Map<String, RateState> m_oRateStates = Collections.synchronizedMap(new HashMap<String, RateState>());
	
	public void addRate(final RateState oRateState)
	{
		m_oRateStates.put(oRateState.getRateInfo().toString(), oRateState);
	}
	
	public Map<String, RateState> getRateStates()
	{
		return m_oRateStates;
	}
	
	public RateState getRate(final RateInfo oRateInfo)
	{
		return m_oRateStates.get(oRateInfo.toString());
	}
}

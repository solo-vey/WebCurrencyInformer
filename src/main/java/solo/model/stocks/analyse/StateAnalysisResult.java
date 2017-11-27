package solo.model.stocks.analyse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import solo.model.stocks.BaseObject;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockRateStates;

public class StateAnalysisResult extends BaseObject
{
	final protected Map<RateInfo, RateAnalysisResult> m_oRatesAnalysisResult = new HashMap<RateInfo, RateAnalysisResult>();
	
	public StateAnalysisResult(final StockRateStates oStockRateStates, final IStockExchange oStockExchange) throws Exception
	{
		for(final RateInfo oRateInfo : oStockExchange.getStockSource().getRates())
			m_oRatesAnalysisResult.put(oRateInfo, new RateAnalysisResult(oStockRateStates, oRateInfo, oStockExchange));
	}
	
	public Set<RateInfo> getRates()
	{
		return m_oRatesAnalysisResult.keySet();
	}
	
	public RateAnalysisResult getRateAnalysisResult(final RateInfo oRateInfo)
	{
		return m_oRatesAnalysisResult.get(oRateInfo);
	}
}

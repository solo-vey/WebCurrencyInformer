package solo.model.stocks.history;

import java.util.LinkedList;
import java.util.List;

import solo.model.stocks.BaseObject;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.oracle.IRateOracle;
import solo.model.stocks.oracle.RatesForecast;

public class StockRateStatesLocalHistory extends BaseObject
{
	final protected int m_nMaxHistorySize; 
	final protected int m_nMaxFutureSize; 
	final protected IRateOracle m_oRateOracle;
	
	final protected List<StateAnalysisResult> m_oHistory = new LinkedList<StateAnalysisResult>();
	final protected List<RatesForecast> m_oFuture = new LinkedList<RatesForecast>();
	
	public StockRateStatesLocalHistory(final int nMaxHistorySize, final int nMaxFutureSize, final IRateOracle oRateOracle)
	{
		m_nMaxHistorySize = nMaxHistorySize;
		m_nMaxFutureSize = nMaxFutureSize;
		m_oRateOracle = oRateOracle;
	}
	
	public void addToHistory(final StateAnalysisResult oStateAnalysisResult)
	{
		m_oHistory.add(oStateAnalysisResult);
		while (m_oHistory.size() > m_nMaxHistorySize)
			m_oHistory.remove(0);

		makeFuture(oStateAnalysisResult);
	}

	private void makeFuture(final StateAnalysisResult oStateAnalysisResult)
	{
		m_oFuture.clear();
		final List<RatesForecast> oRatesForecast = m_oRateOracle.makeForecast(m_oHistory, m_nMaxFutureSize);
		m_oFuture.addAll(oRatesForecast);
	}
	
	public List<StateAnalysisResult> getList()
	{
		return m_oHistory;
	}
	
	public StateAnalysisResult getLastAnalysisResult()
	{
		return (m_oHistory.size() > 0 ? m_oHistory.get(m_oHistory.size() - 1) : null);
	}
	
	public List<RatesForecast> getFuture()
	{
		return m_oFuture;
	}
}

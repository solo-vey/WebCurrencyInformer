package solo.model.stocks.history;

import java.util.LinkedList;
import java.util.List;

import solo.model.stocks.BaseObject;
import solo.model.stocks.analyse.StateAnalysisResult;

public class StockRateStatesLocalHistory extends BaseObject
{
	final protected int m_nMaxHistorySize; 
	final protected List<StateAnalysisResult> m_oHistory = new LinkedList<StateAnalysisResult>();
	
	public StockRateStatesLocalHistory(final int nMaxHistorySize)
	{
		m_nMaxHistorySize = nMaxHistorySize;
	}
	
	public void addToHistory(final StateAnalysisResult oStateAnalysisResult)
	{
		m_oHistory.add(oStateAnalysisResult);
		while (m_oHistory.size() > m_nMaxHistorySize)
			m_oHistory.remove(0);
	}
	
	public List<StateAnalysisResult> getList()
	{
		return m_oHistory;
	}
	
	public StateAnalysisResult getLastAnalysisResult()
	{
		return (m_oHistory.size() > 0 ? m_oHistory.get(m_oHistory.size() - 1) : null);
	}
}

package solo.model.stocks.oracle;

import java.util.List;

import solo.model.stocks.analyse.StateAnalysisResult;

public interface IRateOracle
{
	List<RatesForecast> makeForecast(final List<StateAnalysisResult> oStateAnalysisHistory, final int m_nMaxFutureSize);
}

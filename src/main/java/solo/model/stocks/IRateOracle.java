package solo.model.stocks;

import java.util.List;

public interface IRateOracle
{
	List<RatesForecast> makeForecast(final List<StateAnalysisResult> oStateAnalysisHistory, final int m_nMaxFutureSize);
}

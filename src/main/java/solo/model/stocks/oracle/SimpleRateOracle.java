package solo.model.stocks.oracle;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.RateInfo;

public class SimpleRateOracle implements IRateOracle
{
	protected final String m_oName = "SimpleRateOracle";
	
	@Override public List<RatesForecast> makeForecast(final List<StateAnalysisResult> oStateAnalysisHistory, final int nMaxFutureSize)
	{
		final List<RatesForecast> oResult = new LinkedList<RatesForecast>();
		if (0 == oStateAnalysisHistory.size())
			return oResult;
		
		for(int i = 0; i < nMaxFutureSize; i++)
			oResult.add(new RatesForecast());
		
		final Set<RateInfo> oRates = oStateAnalysisHistory.get(0).getRates();
		for(final RateInfo oRateInfo : oRates)
		{
			double[] aPrices = new double[oStateAnalysisHistory.size() + nMaxFutureSize];
			for(int nHistoryPosition = 0; nHistoryPosition < oStateAnalysisHistory.size(); nHistoryPosition++)
			{
				final StateAnalysisResult oStateAnalysisResult = oStateAnalysisHistory.get(nHistoryPosition);
				aPrices[nHistoryPosition] = oStateAnalysisResult.getRateAnalysisResult(oRateInfo).getAsksAnalysisResult().getAverageAllSumPrice().doubleValue();
				System.out.print(oRateInfo + " Price[" + nHistoryPosition + "] = " + aPrices[nHistoryPosition] + "\r\n");
			}
			
			int nStart = oStateAnalysisHistory.size();
			for(int nForecastPosition = 0; nForecastPosition < nMaxFutureSize; nForecastPosition++)
			{
				final double nForecastPrice = getForecast(aPrices, oStateAnalysisHistory.size() + nForecastPosition, nForecastPosition);
				System.out.print(oRateInfo + " ForecastPrice[" + nForecastPosition + "] = " + nForecastPrice + "\r\n");
				aPrices[nStart + nForecastPosition] = nForecastPrice;

				final RateForecast oRateForecast = new RateForecast(oRateInfo, nForecastPrice);
				oResult.get(nForecastPosition).addForecust(oRateForecast);
			}
		}
		
		return oResult;
	}

	static double getForecast(double[] aPrices, int nCount, int nForecastPosition)
	{
		double nLastPrice = aPrices[nCount - 1];
		if (nCount == 0)
			return 0;
		
		if (nCount == 1)
			return nLastPrice;
		
		final double nNormalize = Math.log10(nCount + 1);
		double nSumKoeff = 0; 
		double nSumDelta = 0;
		for(int nPos = nCount - 2; nPos >= 0; nPos--)
		{
			final double nK = 1 - Math.log10(nCount - nPos)/nNormalize;
			final double nPrice = aPrices[nPos];
			final double nDelta = (nLastPrice - nPrice);
			nSumDelta += nDelta * nK;
			nSumKoeff += nK;
		}
		return nLastPrice + (nSumDelta > 0 ? nSumDelta / nSumKoeff / (1 + nForecastPosition) : 0.0);
	}
}

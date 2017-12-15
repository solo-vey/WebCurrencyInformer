package solo.model.stocks.oracle;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.TrendType;

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
				aPrices[nHistoryPosition] = oStateAnalysisResult.getRateAnalysisResult(oRateInfo).getTradesAnalysisResult().getAverageAllSumPrice().doubleValue();
			}
			
			int nStart = oStateAnalysisHistory.size();
			for(int nForecastPosition = 0; nForecastPosition < nMaxFutureSize; nForecastPosition++)
			{
				final int nLastPos = oStateAnalysisHistory.size() + nForecastPosition;
				final double nForecastPrice = getForecast(aPrices, nLastPos, nForecastPosition);
				aPrices[nStart + nForecastPosition] = nForecastPrice;

				final RateForecast oRateForecast = new RateForecast(oRateInfo, nForecastPrice, getTrendType(aPrices, nLastPos, nForecastPrice));
				oResult.get(nForecastPosition).addForecust(oRateForecast);
			}
		}
		
		return oResult;
	}

	protected TrendType getTrendType(final double[] aPrices, final int nCount, final double nForecastPrice)
	{
		final int nLookbackCount = 10;
		if (nCount < nLookbackCount)
			return TrendType.CALM;
		
		double nLookbackPrice = aPrices[nCount - nLookbackCount - 1];
		double nLastPrice = aPrices[nCount  - 1];
		double nDelta = nForecastPrice - nLookbackPrice;
		double nOnePercent = nLastPrice * 0.01;
		double nQuarterPercent = nOnePercent / 4;

		if (nDelta > nQuarterPercent && nDelta <= nOnePercent) 
			return TrendType.GROWTH;

		if (nDelta > nOnePercent) 
			return TrendType.FAST_GROWTH;

		if (nDelta < -nQuarterPercent && nDelta >= -nOnePercent) 
			return TrendType.FALL;

		if (nDelta < -nOnePercent) 
			return TrendType.FAST_FALL;

		return TrendType.CALM;
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

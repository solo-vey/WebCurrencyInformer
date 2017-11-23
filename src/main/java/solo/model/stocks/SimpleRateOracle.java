package solo.model.stocks;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
			double[] x = new double[oStateAnalysisHistory.size() + nMaxFutureSize];
			double[] y = new double[oStateAnalysisHistory.size() + nMaxFutureSize];
			for(int i = 0; i < oStateAnalysisHistory.size(); i++)
			{
				x[i] = i;
				final StateAnalysisResult oStateAnalysisResult = oStateAnalysisHistory.get(i);
				final double nLevelPrice = oStateAnalysisResult.getRateAnalysisResult(oRateInfo).getAsksAnalysisResult().getBestPrice().doubleValue();
				y[i] = nLevelPrice;
				System.out.print(oRateInfo + " Price[" + i + "] = " + nLevelPrice + "\r\n");
			}
			
			if (oStateAnalysisHistory.size() > 19)
			{
				int i = 99;
				i = 99;
			}

			
			int nStart = oStateAnalysisHistory.size();
			for(int i = 0; i < nMaxFutureSize; i++)
			{
				final double nForecastPrice = getForecast(nStart + i, x, y, oStateAnalysisHistory.size());
				System.out.print(oRateInfo + " ForecastPrice[" + i + "] = " + nForecastPrice + "\r\n");
				y[nStart + i] = nForecastPrice;

				final RateForecast oRateForecast = new RateForecast(oRateInfo, nForecastPrice);
				oResult.get(i).addForecust(oRateForecast);
			}
		}
		
		return oResult;
	}

	static double getForecast(double x, double[] xValues, double[] yValues, int size)
	{
		final double nC = Math.log10(size + 1);
		double nSumK = 0; 
		double nResult = 0;
		for(int i = 0; i < size; i++)
		{
			final double nK = 1 - Math.log10(i + 1)/nC;
			nResult += yValues[i] * nK;
			nSumK += nK;
		}
		return nResult / nSumK;
	}
	
    static double InterpolateLagrangePolynomial (double x, double[] xValues, double[] yValues, int size)
    {
        double lagrangePol = 0;

        for (int i = 0; i < size; i++)
        {
                double basicsPol = 1;
                for (int j = 0; j < size; j++)
                {
                    if (j != i)
                    {
                        basicsPol *= (x - xValues[j])/(xValues[i] - xValues[j]);
                    }
                }
                lagrangePol += basicsPol * yValues[i];
        }

        return lagrangePol;
    }
}

package solo.model.stocks.item.analyse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.FixedMillisecond;

import solo.CurrencyInformer;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.utils.MathUtils;
import ua.lz.ep.utils.ResourceUtils;

public class Candlestick implements Serializable
{
	private static final long serialVersionUID = 6106531883802992172L;
	
	final protected List<JapanCandle> m_oHistory = new LinkedList<JapanCandle>();
	final protected Integer m_nCandleDurationMinutes;
	final protected Integer m_nHistoryLength = 100;
	final protected RateInfo m_oRateInfo;
	final protected String m_strStockExchangeName;
	
	public Candlestick(final IStockExchange oStockExchange, final int nCandleDurationMinutes, final RateInfo oRateInfo)
	{
		m_strStockExchangeName = oStockExchange.getStockName();
		m_nCandleDurationMinutes = nCandleDurationMinutes;
		m_oRateInfo = oRateInfo;
	}
	
	public void addRateInfo(RateAnalysisResult oRateAnalysisResult)
	{
		if (m_oHistory.size() == 0)
			m_oHistory.add(new JapanCandle());
		
		JapanCandle oCandle = m_oHistory.get(m_oHistory.size() - 1);
		final Date oLastCandleDate = DateUtils.addMinutes(oCandle.getDate(), m_nCandleDurationMinutes); 
		if (oLastCandleDate.compareTo(new Date()) <= 0)
		{
			oCandle = new JapanCandle(); 
			m_oHistory.add(oCandle);
		}
		
		while (m_oHistory.size() >= m_nHistoryLength)
			m_oHistory.remove(0);
		
		oCandle.setValue(oRateAnalysisResult.getTradesAnalysisResult().getAverageAllSumPrice());
	}
	
	public BigDecimal getAverageMinPrice()
	{
		BigDecimal nSumaryMinPrice = BigDecimal.ZERO;
		int nStepCount = 1;
		CandleType oCurrentCandleType = CandleType.NONE;
        for(int nPos = 0; nPos < m_oHistory.size(); nPos++)
        {
        	final JapanCandle oCandle = m_oHistory.get(m_oHistory.size() - nPos - 1);
        	if (oCurrentCandleType.equals(CandleType.FALL) && oCandle.getCandleType().equals(CandleType.GROW))
        	{
        		nSumaryMinPrice = nSumaryMinPrice.add(oCandle.getMin());
        		nStepCount++;
        	}
        	oCurrentCandleType = oCandle.getCandleType(); 
        }
        return MathUtils.getBigDecimal(nSumaryMinPrice.doubleValue() / nStepCount, TradeUtils.DEFAULT_PRICE_PRECISION);
	}

	public BigDecimal getAverageMaxPrice()
	{
		BigDecimal nSumaryMaxPrice = BigDecimal.ZERO;
		int nStepCount = 1;
		CandleType oCurrentCandleType = CandleType.NONE;
        for(int nPos = 0; nPos < m_oHistory.size(); nPos++)
        {
        	final JapanCandle oCandle = m_oHistory.get(m_oHistory.size() - nPos - 1);
        	if (oCurrentCandleType.equals(CandleType.GROW) && oCandle.getCandleType().equals(CandleType.FALL))
        	{
        		nSumaryMaxPrice = nSumaryMaxPrice.add(oCandle.getMax());
        		nStepCount++;
        	}
        	oCurrentCandleType = oCandle.getCandleType(); 
        }
        return MathUtils.getBigDecimal(nSumaryMaxPrice.doubleValue() / nStepCount, TradeUtils.DEFAULT_PRICE_PRECISION);
	}
	
	public String makeChartImage() throws IOException
	{
		final JFreeChart oChart = JfreeCandlestickChart.createChart(m_oRateInfo.toString(), m_oHistory);
    	ChartUtilities.saveChartAsJPEG(new File(getFileName()), oChart, 480, 240);
    	return getFileName();
	}

	public String getFileName()
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_strStockExchangeName + "\\" + m_oRateInfo + ".jpeg";
	}
}

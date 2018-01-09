package solo.model.stocks.item.analyse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import solo.CurrencyInformer;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
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
	
	public String makeChartImage() throws IOException
	{
		final JFreeChart oChart = JfreeCandlestickChart.createChart(m_oRateInfo.toString(), m_oHistory);
  //  	final JFreeChart oChart = ChartFactory.createCandlestickChart(m_oRateInfo.toString(), StringUtils.EMPTY, StringUtils.EMPTY, oDataset, false);
    	ChartUtilities.saveChartAsJPEG(new File(getFileName()), oChart, 480, 240);
    	return getFileName();
	}

	public String getFileName()
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_strStockExchangeName + "\\" + m_oRateInfo + ".jpeg";
	}
}

package solo.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultHighLowDataset;

import solo.model.stocks.item.analyse.JapanCandle;

public class CandlestickUtils
{
	CandlestickUtils() 
	{
		throw new IllegalStateException("Utility class");
	}
	
    public void makeImage(final List<JapanCandle> aCandles, final String strTitle, final String strFile) throws IOException
    {
    	
		final DefaultHighLowDataset dataset = createHighLowDataset(aCandles);
    	final JFreeChart chart = ChartFactory.createCandlestickChart(strTitle, StringUtils.EMPTY, StringUtils.EMPTY, dataset, false);
    	ChartUtilities.saveChartAsJPEG(new File("c:\\1.jpg"), chart, 1024, 800);
   	}

	private DefaultHighLowDataset createHighLowDataset(List<JapanCandle> aCandles)
	{
		return null;
	}
}

package solo.model.stocks.item.analyse;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;

/**
 * The Class JfreeCandlestickChart.
 * 
 * @author ashraf
 */
@SuppressWarnings("serial")
public class JfreeCandlestickChart extends JPanel 
{
	public static JFreeChart createChart(String chartTitle, List<JapanCandle> oHistory, final int nDurationMinutes) 
	{
    	final int nScale = getScale(oHistory);
		// Create OHLCSeriesCollection as a price dataset for candlestick chart
		OHLCSeriesCollection candlestickDataset = new OHLCSeriesCollection();
		OHLCSeries ohlcSeries = new OHLCSeries("Price" + (nScale > 1 ? " (" + nScale + ")" : StringUtils.EMPTY));
		candlestickDataset.addSeries(ohlcSeries);
		createHighLowDataset(ohlcSeries, oHistory, nDurationMinutes);
		
		// Create candlestick chart priceAxis
		NumberAxis priceAxis = new NumberAxis("Price" + (nScale > 1 ? " (" + nScale + ")" : StringUtils.EMPTY));
		priceAxis.setAutoRangeIncludesZero(false);
		// Create candlestick chart renderer
		CandlestickRenderer candlestickRenderer = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE,
				false, new HighLowItemLabelGenerator(new SimpleDateFormat("kk:mm"), new DecimalFormat("0.000")));
		// Create candlestickSubplot
		XYPlot candlestickSubplot = new XYPlot(candlestickDataset, null, priceAxis, candlestickRenderer);
		candlestickSubplot.setBackgroundPaint(Color.white);

		// creates TimeSeriesCollection as a volume dataset for volume chart
/*		TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
		TimeSeries volumeSeries = new TimeSeries("Volume");
		volumeDataset.addSeries(volumeSeries);
		// Create volume chart volumeAxis
		NumberAxis volumeAxis = new NumberAxis("Volume");
		volumeAxis.setAutoRangeIncludesZero(false);
		// Set to no decimal
		volumeAxis.setNumberFormatOverride(new DecimalFormat("0"));
		// Create volume chart renderer
		XYBarRenderer timeRenderer = new XYBarRenderer();
		timeRenderer.setShadowVisible(false);
		timeRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("Volume--> Time={1} Size={2}",
				new SimpleDateFormat("kk:mm"), new DecimalFormat("0")));
		// Create volumeSubplot
		XYPlot volumeSubplot = new XYPlot(volumeDataset, null, volumeAxis, timeRenderer);
		volumeSubplot.setBackgroundPaint(Color.white);*/

		// Creating charts common dateAxis
		DateAxis dateAxis = new DateAxis("Time");
		dateAxis.setDateFormatOverride(new SimpleDateFormat("kk:mm"));
		// reduce the default left/right margin from 0.05 to 0.02
		dateAxis.setLowerMargin(0.02);
		dateAxis.setUpperMargin(0.02);

		CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
		mainPlot.setGap(10.0);
		mainPlot.add(candlestickSubplot, 3);
//		mainPlot.add(volumeSubplot, 1);
		mainPlot.setOrientation(PlotOrientation.VERTICAL);

		JFreeChart chart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
		chart.removeLegend();
		return chart;
	}
	
	
    public static void createHighLowDataset(OHLCSeries oOhlcSeries, List<JapanCandle> oHistory, int nDurationMinutes) 
    {
    	if (oHistory.size() == 0)
    		return;
    	
    	final int nScale = getScale(oHistory);
    	final Date oMinDate = DateUtils.addMinutes(new Date(), -nDurationMinutes);
        for(int nPos = oHistory.size() - 1; nPos > 0; nPos--)
        {
        	final JapanCandle oCandle = oHistory.get(nPos);
        	if (oCandle.getDate().before(oMinDate))
        		break;
        	
        	final FixedMillisecond oTime = new FixedMillisecond(oCandle.getDate().getTime());
        	oOhlcSeries.add(oTime, oCandle.getStart().doubleValue() * nScale, oCandle.getMax().doubleValue() * nScale, 
        					oCandle.getMin().doubleValue() * nScale, oCandle.getEnd().doubleValue() * nScale);
        }
    }

    public static int getScale(final List<JapanCandle> oHistory) 
    {
    	if (oHistory.size() == 0)
    		return 1;
    	
    	final JapanCandle oLastCandle = oHistory.get(oHistory.size() - 1);
    	if (oLastCandle.getMin().compareTo(new BigDecimal(1)) < 0.001)
    		return 1000 * 1000;
    	else if (oLastCandle.getMin().compareTo(new BigDecimal(1)) < 1)
    		return 1000;
    	
    	return 1;
    }
}

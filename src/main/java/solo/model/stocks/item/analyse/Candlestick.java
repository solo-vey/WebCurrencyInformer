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
import solo.CurrencyInformer;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.utils.MathUtils;
import ua.lz.ep.utils.ResourceUtils;

public class Candlestick implements Serializable
{
	private static final long serialVersionUID = 6106531883802992172L;

	final static public int CANDLE_HISTORY_SIZE = 300;
	
	final protected List<JapanCandle> m_oHistory = new LinkedList<JapanCandle>();
	final protected Integer m_nCandleDurationMinutes;
	final protected RateInfo m_oRateInfo;
	final protected String m_strStockExchangeName;
	
	protected Date m_oLastOrderDate = null;
	
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
			final BigDecimal nLastCandleEnd = oCandle.getEnd(); 
			oCandle = new JapanCandle(); 
			m_oHistory.add(oCandle);
			oCandle.setValue(nLastCandleEnd);
		}
		
		while (m_oHistory.size() >= CANDLE_HISTORY_SIZE)
			m_oHistory.remove(0);
		
		for(int nPos = oRateAnalysisResult.getTrades().size() - 1; nPos >= 0; nPos --)
		{
			final Order oOrder = oRateAnalysisResult.getTrades().get(nPos);
			if (null == m_oLastOrderDate || m_oLastOrderDate.before(oOrder.getCreated()))
				oCandle.setValue(oOrder.getPrice());
		}
		m_oLastOrderDate = oRateAnalysisResult.getTrades().get(0).getCreated();
	}
	
	public BigDecimal getAverageMinPrice(int nStepCount)
	{
		BigDecimal nSumaryMinPrice = BigDecimal.ZERO;
		nStepCount = (-1 == nStepCount ? m_oHistory.size() : nStepCount);
        for(int nPos = 0; nPos < m_oHistory.size() && nPos < nStepCount; nPos++)
        {
        	final JapanCandle oCandle = m_oHistory.get(m_oHistory.size() - nPos - 1);
       		nSumaryMinPrice = nSumaryMinPrice.add(oCandle.getMin());
        }
        return MathUtils.getBigDecimal(nSumaryMinPrice.doubleValue() / nStepCount, TradeUtils.DEFAULT_PRICE_PRECISION);
	}

	public BigDecimal getAverageMaxPrice(int nStepCount)
	{
		BigDecimal nSumaryMaxPrice = BigDecimal.ZERO;
		nStepCount = (-1 == nStepCount ? m_oHistory.size() : nStepCount);
        for(int nPos = 0; nPos < m_oHistory.size() && nPos < nStepCount; nPos++)
        {
        	final JapanCandle oCandle = m_oHistory.get(m_oHistory.size() - nPos - 1);
       		nSumaryMaxPrice = nSumaryMaxPrice.add(oCandle.getMax());
        }
        return MathUtils.getBigDecimal(nSumaryMaxPrice.doubleValue() / nStepCount, TradeUtils.DEFAULT_PRICE_PRECISION);
	}

	public BigDecimal getMinMaxDelta(int nStepCount)
	{
		BigDecimal nMaxPrice = BigDecimal.ZERO;
		BigDecimal nMinPrice = BigDecimal.ZERO;
		nStepCount = (-1 == nStepCount ? m_oHistory.size() : nStepCount);
        for(int nPos = 0; nPos < m_oHistory.size() && nPos < nStepCount; nPos++)
        {
        	final JapanCandle oCandle = m_oHistory.get(m_oHistory.size() - nPos - 1);
        	if (nMaxPrice.compareTo(oCandle.getMax()) < 0)
        		nMaxPrice = oCandle.getMax();
        	if (nMinPrice.equals(BigDecimal.ZERO) || nMinPrice.compareTo(oCandle.getMin()) > 0)
        		nMinPrice = oCandle.getMin();
        }
        
        final BigDecimal nDelta = nMaxPrice.add(nMinPrice.negate());
        return MathUtils.getBigDecimal(nDelta.doubleValue() * 100.0 / nMaxPrice.doubleValue(), 2);
	}

	public BigDecimal getMax(int nStepCount)
	{
		BigDecimal nMaxPrice = BigDecimal.ZERO;
		nStepCount = (-1 == nStepCount ? m_oHistory.size() : nStepCount);
        for(int nPos = 0; nPos < m_oHistory.size() && nPos < nStepCount; nPos++)
        {
        	final JapanCandle oCandle = m_oHistory.get(m_oHistory.size() - nPos - 1);
        	if (nMaxPrice.compareTo(oCandle.getMax()) < 0)
        		nMaxPrice = oCandle.getMax();
        }
        
        return nMaxPrice;
	}
	
	public String makeChartImage(final int nStepCount) throws IOException
	{
		final JFreeChart oChart = JfreeCandlestickChart.createChart(m_oRateInfo.toString() + " - " + getType(), m_oHistory, m_nCandleDurationMinutes * nStepCount);
    	ChartUtilities.saveChartAsJPEG(new File(getFileName()), oChart, 480, 320);
    	return getFileName();
	}

	public String getFileName()
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_strStockExchangeName + "\\" + m_oRateInfo + ".jpeg";
	}
	
	public List<String> getHistoryInfo()
	{
		final List<String> aResult = new LinkedList<String>();
        for(int nPos = 0; nPos < m_oHistory.size() - 3; nPos++)
        {
        	final JapanCandle oCandle = m_oHistory.get(m_oHistory.size() - nPos - 1);
        	
			final CandlestickType oType = getType(nPos);
			final boolean bIsLongFall = oType.equals(CandlestickType.THREE_BLACK) || oType.equals(CandlestickType.TWO_BLACK) || oType.equals(CandlestickType.WHITE_AND_TWO_BLACK);
			final boolean bIsLongGrowth = oType.equals(CandlestickType.THREE_WHITE) || oType.equals(CandlestickType.TWO_WHITE) || oType.equals(CandlestickType.BLACK_AND_TWO_WHITE);
			final String strLongType = (bIsLongFall ? "LongFall" : bIsLongGrowth ? "LongGrowth" : "LongCalm");
			aResult.add(0, oCandle.getDate().toString() + " -> " + strLongType + " - " + oType + " - " + oCandle.getCandleType() + " - " + oCandle.toString());
        }
        
        return aResult;
	}

	public CandlestickType getType()
	{
		return getType(0); 
	}
	
	public boolean isLongFall()
	{
		final BigDecimal nMinMaxDeltaPercent = getMinMaxDelta(6);
		if (nMinMaxDeltaPercent.compareTo(new BigDecimal(2)) < 0)
			return false;
		
		return getType().equals(CandlestickType.THREE_BLACK) || 
				getType().equals(CandlestickType.TWO_BLACK) || 
				getType().equals(CandlestickType.WHITE_AND_TWO_BLACK);
	}
	
	public boolean isLongGrowth()
	{
		final BigDecimal nMinMaxDeltaPercent = getMinMaxDelta(6);
		if (nMinMaxDeltaPercent.compareTo(new BigDecimal(2)) < 0)
			return false;
		
		return getType().equals(CandlestickType.THREE_WHITE) || 
				getType().equals(CandlestickType.TWO_WHITE) || 
				getType().equals(CandlestickType.BLACK_AND_TWO_WHITE);
	}
	
	public CandlestickType getType(int nStep)
	{
		if (nStep == 0)
		{
	       	final JapanCandle oLastCandle = m_oHistory.get(m_oHistory.size() - 1);
	       	final Date oMinDateStartCandle = DateUtils.addSeconds(new Date(), -m_nCandleDurationMinutes * 60 / 3);
	       	if (oLastCandle.getCandleType().getGroupType().equals(CandleGroupType.DOJI) && oLastCandle.m_oDate.before(oMinDateStartCandle))
	       		nStep++;
		}
		
		if (m_oHistory.size() - nStep < 3)
			return CandlestickType.UNKNOWN;

       	final JapanCandle oCandle3 = m_oHistory.get(m_oHistory.size() - nStep - 1);
       	final JapanCandle oCandle2 = m_oHistory.get(m_oHistory.size() - nStep - 2);
       	final JapanCandle oCandle1 = m_oHistory.get(m_oHistory.size() - nStep - 3);

       	final CandleType oCandleType3 = oCandle3.getCandleType();
       	final CandleType oCandleType2 = oCandle2.getCandleType();
       	final CandleType oCandleType1 = oCandle1.getCandleType();

		//	Три белых солдата        	
		if (isThreeWhite(oCandleType3, oCandleType2, oCandleType1))
			return CandlestickType.THREE_WHITE;

		//	Три черных ворона 
		if (isThreeBlack(oCandleType3, oCandleType2, oCandleType1))
			return CandlestickType.THREE_BLACK;

		//	Утренняя звезда
		if (isMorningStar(oCandleType3, oCandleType2, oCandleType1)) 
			return CandlestickType.MORNING_STAR;

		//	Вечерняя звезда
		if (isEveningStar(oCandleType3, oCandleType2, oCandleType1)) 
			return CandlestickType.EVENING_STAR;

		//	Три внутри вверх
		if (isBlackToWhite(oCandleType3, oCandleType2, oCandleType1)) 
			return CandlestickType.BLACK_AND_TWO_WHITE;

		//	Три внутри вниз
		if (isWhiteToBlack(oCandleType3, oCandleType2, oCandleType1)) 
			return CandlestickType.WHITE_AND_TWO_BLACK;

		//	Бычье поглощение
		if (isBullAbsorption(oCandleType3, oCandleType2)) 
		{
			if (oCandle2.getMax().compareTo(oCandle3.getMax()) < 0)
				return CandlestickType.BLACK_TO_WHITE;
			
			return CandlestickType.CALM;
		}

		//	Медвежье поглощение
		if (isBearAbsorption(oCandleType3, oCandleType2)) 
		{
			if (oCandle2.getMin().compareTo(oCandle3.getMin()) >= 0)
				return CandlestickType.WHITE_TO_BLACK;
			
			return CandlestickType.CALM;
		}
			
		//	Пинцет днища
		if (isBottomPliers(oCandleType3, oCandleType2)) 
			return CandlestickType.BLACK_TO_WHITE;
			
		//	Вершины пинцета
		if (isTopPliers(oCandleType3, oCandleType2)) 
			return CandlestickType.WHITE_TO_BLACK;

		if (isTopPliers(oCandleType3, oCandleType2)) 
			return CandlestickType.WHITE_TO_BLACK;

		//	Спокойно			
		if (isCalm(oCandleType3, oCandleType2, oCandleType1))
			return CandlestickType.CALM;
			
		if (oCandleType1.isGrowth() && oCandleType2.isGrowth() && oCandleType3.isCalm() && oCandle2.getMax().compareTo(oCandle1.getMax()) > 0)
			return CandlestickType.TWO_WHITE;

		if (oCandleType1.isFall() && oCandleType2.isFall() && oCandle2.getMin().compareTo(oCandle1.getMin()) < 0)
			return CandlestickType.TWO_BLACK;

		if (oCandleType2.isGrowth() && oCandleType3.isGrowth())
			return CandlestickType.TWO_WHITE;

		if (oCandleType2.isFall() && oCandleType3.isFall())
			return CandlestickType.TWO_BLACK;

		if (oCandleType1.isGrowth() && oCandleType3.isGrowth())
			return CandlestickType.TWO_WHITE;

		if (oCandleType1.isFall() && oCandleType3.isFall())
			return CandlestickType.TWO_BLACK;
		
		if (oCandleType1.isCalm() && oCandleType2.isFall() && oCandleType3.isCalm())
			return CandlestickType.FALL;

		if (oCandleType1.isCalm() && oCandleType2.isCalm() && oCandleType3.isFall())
			return CandlestickType.FALL;

		if (oCandleType1.isFall() && oCandleType2.isCalm() && oCandleType3.isCalm())
			return CandlestickType.FALL;
		
		if (oCandleType3.isGrowth())
			return CandlestickType.START_GROWTH;

		if (oCandleType3.isFall())
			return CandlestickType.START_FALL;
			
		return CandlestickType.UNKNOWN;
	}

	boolean isCalm(CandleType oCandleType3, CandleType oCandleType2, CandleType oCandleType1)
	{
		if (oCandleType1.getTrendType().equals(TrendType.CALM) &&
				oCandleType2.getTrendType().equals(TrendType.CALM) &&  
				oCandleType3.getTrendType().equals(TrendType.CALM))
			return true;	

		if (oCandleType3.getTrendType().equals(TrendType.CALM) && 
				oCandleType2.getTrendType().equals(TrendType.CALM) && !oCandleType1.isFall())
			return true;	

		return false;
	}

	boolean isTopPliers(final CandleType oCandleType3, final CandleType oCandleType2)
	{
		if (oCandleType2.isGrowth(CandleGroupType.HAMMER) && oCandleType3.isFall(CandleGroupType.HAMMER))
			return true;

		if (oCandleType2.isGrowth(CandleGroupType.STANDARD) && oCandleType3.isFall(CandleGroupType.HAMMER))
			return true;
			
		return false;	
	}

	boolean isBottomPliers(final CandleType oCandleType3, final CandleType oCandleType2)
	{
		if (oCandleType2.isFall(CandleGroupType.HAMMER) && oCandleType3.isGrowth(CandleGroupType.HAMMER))
			return true;

		if (oCandleType2.isFall(CandleGroupType.STANDARD) && oCandleType3.isGrowth(CandleGroupType.HAMMER))
			return true;
			
		return false;	
	}

	boolean isBearAbsorption(final CandleType oCandleType3, final CandleType oCandleType2)
	{
		return oCandleType2.isGrowth(CandleGroupType.STANDARD) &&  
				oCandleType3.isFall(CandleGroupType.STANDARD);
	}

	boolean isBullAbsorption(final CandleType oCandleType3, final CandleType oCandleType2)
	{
		return oCandleType2.isFall(CandleGroupType.STANDARD) &&  
				oCandleType3.isGrowth(CandleGroupType.STANDARD);
	}

	boolean isWhiteToBlack(final CandleType oCandleType3, final CandleType oCandleType2, final CandleType oCandleType1)
	{
		return (oCandleType1.isGrowth(CandleGroupType.STANDARD) && 
				oCandleType2.isFall(CandleGroupType.STANDARD) &&  
				oCandleType3.isFall(CandleGroupType.STANDARD));
	}

	boolean isBlackToWhite(final CandleType oCandleType3, final CandleType oCandleType2, final CandleType oCandleType1)
	{
		return (oCandleType1.isFall(CandleGroupType.STANDARD) && 
				oCandleType2.isGrowth(CandleGroupType.STANDARD) &&  
				oCandleType3.isGrowth(CandleGroupType.STANDARD));
	}

	boolean isEveningStar(final CandleType oCandleType3, final CandleType oCandleType2, final CandleType oCandleType1)
	{
		return (oCandleType1.isGrowth(CandleGroupType.STANDARD) && 
				(oCandleType2.isFall(CandleGroupType.HAMMER) || oCandleType2.isGrowth(CandleGroupType.HAMMER)) &&  
				oCandleType3.isFall(CandleGroupType.STANDARD));
	}

	boolean isMorningStar(final CandleType oCandleType3, final CandleType oCandleType2, final CandleType oCandleType1)
	{
		return (oCandleType1.isFall(CandleGroupType.STANDARD) && 
				(oCandleType2.isFall(CandleGroupType.DOJI) || oCandleType2.isFall(CandleGroupType.HAMMER)) &&  
				oCandleType3.isGrowth(CandleGroupType.STANDARD));
	}

	boolean isThreeWhite(final CandleType oCandleType3, final CandleType oCandleType2, final CandleType oCandleType1)
	{
		return (oCandleType1.isGrowth(CandleGroupType.STANDARD) && 
				oCandleType2.isGrowth(CandleGroupType.STANDARD) &&
				oCandleType3.isGrowth(CandleGroupType.STANDARD));
	}	

	boolean isThreeBlack(final CandleType oCandleType3, final CandleType oCandleType2, final CandleType oCandleType1)
	{
		if (oCandleType1.isFall(CandleGroupType.STANDARD) && 
				oCandleType2.isFall(CandleGroupType.STANDARD) &&
				oCandleType3.isFall(CandleGroupType.STANDARD))
			return true;
			
		return false;
	}	
}

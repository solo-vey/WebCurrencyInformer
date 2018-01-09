package solo.model.stocks.item.analyse;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import solo.CurrencyInformer;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import ua.lz.ep.utils.ResourceUtils;

public class StockCandlestick implements Serializable
{
	private static final long serialVersionUID = -6372684938216571748L;
	
	final Map<RateInfo, Candlestick> m_oRateCandlestick = Collections.synchronizedMap(new HashMap<RateInfo, Candlestick>());
	final protected IStockExchange m_oStockExchange;
	final protected int m_nCandleDurationMinutes;
	protected int m_nAddCount = 0;
	
	public StockCandlestick(final IStockExchange oStockExchange, final int nCandleDurationMinutes)
	{
		m_oStockExchange = oStockExchange;
		m_nCandleDurationMinutes = nCandleDurationMinutes;
		load();
	}
	
	public void addRateInfo(final RateInfo oRateInfo, final RateAnalysisResult oRateAnalysisResult)
	{
		if (!m_oRateCandlestick.containsKey(oRateInfo))
			m_oRateCandlestick.put(oRateInfo, new Candlestick(m_oStockExchange, m_nCandleDurationMinutes, oRateInfo));
		m_oRateCandlestick.get(oRateInfo).addRateInfo(oRateAnalysisResult);
		
		m_nAddCount++;
		if (m_nAddCount % 10 == 0)
			save();
	}
	
	public Candlestick get(final RateInfo oRateInfo)
	{
		return m_oRateCandlestick.get(oRateInfo);
	}
	
	public void save()
	{
		final String strStockEventsFileName = getFileName();

		try 
		{
	         final FileOutputStream oFileStream = new FileOutputStream(strStockEventsFileName);
	         final ObjectOutputStream oStream = new ObjectOutputStream(oFileStream);
	         oStream.writeObject(m_oRateCandlestick);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (IOException e) 
		{
			System.err.printf("Save stock candlestick exception : " + e.getMessage());
		}			
	}

	@SuppressWarnings("unchecked")
	public void load()
	{
		final String strStockEventsFileName = getFileName(); 
		try 
		{
	         final FileInputStream oFileStream = new FileInputStream(strStockEventsFileName);
	         final ObjectInputStream oStream = new ObjectInputStream(oFileStream);
	         final Map<RateInfo, Candlestick> oRateCandlestick = (Map<RateInfo, Candlestick>) oStream.readObject();
	         m_oRateCandlestick.clear();
	         m_oRateCandlestick.putAll(oRateCandlestick);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (final Exception e) 
		{
			System.err.printf("Load stock candlestick exception : " + e.getMessage() + "\r\n");
	    }			
	}

	String getFileName()
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_oStockExchange.getStockName() + "\\Candlestick.ser";
	}
}

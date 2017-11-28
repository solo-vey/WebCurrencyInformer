package solo.model.stocks.item;

import java.math.BigDecimal;
import solo.model.stocks.BaseObject;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.utils.MathUtils;

public class Event extends BaseObject
{
	final protected EventType m_oType;
	final protected RateInfo m_oRateInfo;
	final protected BigDecimal m_nPrice;
	protected String m_strMessage;
	protected Boolean m_bIsOccurred = false; 
	
	public Event(final EventType oType, final RateInfo oRateInfo, final BigDecimal nPrice)
	{
		m_oType = oType;
		m_oRateInfo = oRateInfo;
		m_nPrice = nPrice;
	}
	
	public EventType getType()
	{
		return m_oType;
	}
	
	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}
	
	public BigDecimal getPrice()
	{
		return m_nPrice;
	}
	
	public Boolean getIsOccurred()
	{
		return m_bIsOccurred;
	}
	
	public String getMessage()
	{
		return m_strMessage;
	}
	
	public String getInfo()
	{
		return "Event " + getType() + " " + getRateInfo().getCurrencyFrom() + ", price " + MathUtils.toCurrencyString(getPrice(), getRateInfo().getCurrencyTo()) + " " + 
		"/deleteEvent_" + getRateInfo().getCurrencyFrom() + "_" +  getType() + "_" + getPrice() + "\r\n";   
	}
	
	public boolean check(final StateAnalysisResult oStateAnalysisResult )
	{
		final RateAnalysisResult oRateAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
		final BigDecimal oCurrentPrice = oRateAnalysisResult.getBidsAnalysisResult().getAverageAllSumPrice();
		if (m_oType.equals(EventType.SELL) && oCurrentPrice.compareTo(m_nPrice) > 0)
		{
			m_bIsOccurred = true;
			m_strMessage = "Event SELL " + getRateInfo().getCurrencyFrom() + ", price " + MathUtils.toCurrencyString(getPrice(), getRateInfo().getCurrencyTo()) + " is occurred\r\n" + 
				"Current price " + MathUtils.toCurrencyString(oCurrentPrice, getRateInfo().getCurrencyTo()) ;   
			return true;
		}

		if (m_oType.equals(EventType.BUY) && oRateAnalysisResult.getBidsAnalysisResult().getAverageAllSumPrice().compareTo(m_nPrice) < 0)
		{
			m_strMessage = "Event BUY " + getRateInfo().getCurrencyFrom() + ", price " + MathUtils.toCurrencyString(getPrice(), getRateInfo().getCurrencyTo()) + " is occurred\r\n" + 
				"Current price " + MathUtils.toCurrencyString(oCurrentPrice, getRateInfo().getCurrencyTo()) ;   
			m_bIsOccurred = true;
			return true;
		}
		
		return false;
	}
}


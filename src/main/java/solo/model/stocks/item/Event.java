package solo.model.stocks.item;

import java.math.BigDecimal;
import solo.model.stocks.BaseObject;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.utils.MathUtils;

public class Event extends BaseObject implements IRule
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
	
	public boolean getIsOccurred()
	{
		return m_bIsOccurred;
	}
	
	public String getMessage()
	{
		return m_strMessage;
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return getType() + "/" + getRateInfo().getCurrencyFrom() + "/" + MathUtils.toCurrencyString(getPrice()) + " /deleteEvent_" + nRuleID + "\r\n";   
	}
	
	public boolean check(final StateAnalysisResult oStateAnalysisResult)
	{
		final RateAnalysisResult oRateAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
		final BigDecimal oBidPrice = oRateAnalysisResult.getBidsAnalysisResult().getAverageAllSumPrice();
		final BigDecimal oAskPrice = oRateAnalysisResult.getAsksAnalysisResult().getAverageAllSumPrice();
		if (m_oType.equals(EventType.SELL) && oBidPrice.compareTo(m_nPrice) >= 0)
		{
			m_bIsOccurred = true;
			m_strMessage = "Event SELL/" + getRateInfo().getCurrencyFrom() + "/" + MathUtils.toCurrencyString(getPrice()) + " is occurred\r\n" + 
				"Current price " + MathUtils.toCurrencyString(oBidPrice) ;   
			return true;
		}

		if (m_oType.equals(EventType.BUY) && oAskPrice.compareTo(m_nPrice) <= 0)
		{
			m_strMessage = "Event BUY/" + getRateInfo().getCurrencyFrom() + "/" + MathUtils.toCurrencyString(getPrice()) + " is occurred\r\n" + 
				"Current price " + MathUtils.toCurrencyString(oAskPrice) ;   
			m_bIsOccurred = true;
			return true;
		}

		final BigDecimal oTradePrice = oRateAnalysisResult.getTradesAnalysisResult().getAverageAllSumPrice();
		final BigDecimal oTradeMinPrice = new BigDecimal(getPrice().doubleValue() * 0.9975); 
		final BigDecimal oTradeMaxPrice = new BigDecimal(getPrice().doubleValue() * 1.0025); 
		if (m_oType.equals(EventType.TRADE) && oTradePrice.compareTo(oTradeMinPrice) >= 0 && oTradePrice.compareTo(oTradeMaxPrice) <= 0)
		{
			m_strMessage = "Event TRADE/" + getRateInfo().getCurrencyFrom() + "/" + MathUtils.toCurrencyString(getPrice()) + " is occurred\r\n" + 
				MathUtils.toCurrencyString(oTradeMinPrice) + "/Current " + MathUtils.toCurrencyString(oTradePrice) + "/" + MathUtils.toCurrencyString(oTradeMaxPrice) + "\r\n" ;   
			m_bIsOccurred = true;
			return true;
		}
		
		return false;
	}
}


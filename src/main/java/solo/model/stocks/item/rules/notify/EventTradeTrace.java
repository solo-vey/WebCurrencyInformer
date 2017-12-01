package solo.model.stocks.item.rules.notify;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.RateInfo;

public class EventTradeTrace extends EventTrade
{
	String m_strMoveType = StringUtils.EMPTY;
		
	public EventTradeTrace(final RateInfo oRateInfo, final String strPriceInfo)
	{
		super(oRateInfo, strPriceInfo);
	}

	@Override public String getType()
	{
		return m_strMoveType + "TradeTrace";   
	}
	
	boolean checkCurrentPrice(final BigDecimal oTradePrice, final BigDecimal oTradeMinPrice, final BigDecimal oTradeMaxPrice)
	{
		return oTradePrice.compareTo(oTradeMinPrice) <= 0 || oTradePrice.compareTo(oTradeMaxPrice) >= 0;
	}
	
	public void onOccurred(final BigDecimal nPrice, final Integer nRuleID)
	{
		m_strMoveType = (nPrice.compareTo(m_nPrice) > 0 ? "[^]" : "[v]");
		super.onOccurred(nPrice, null);
		m_nPrice = nPrice;
	}
}


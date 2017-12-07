package solo.model.stocks.item.rules.notify;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.RateInfo;

public class EventSellTrace extends EventSell
{
	private static final long serialVersionUID = -5138333232638394248L;

	final static public String DELTA_PARAMETER = "#delta#";
	
	String m_strMoveType = StringUtils.EMPTY;
	protected BigDecimal m_nDelta;
		
	public EventSellTrace(final RateInfo oRateInfo, final String strPriceInfo)
	{
		super(oRateInfo, strPriceInfo, DELTA_PARAMETER);
		m_nDelta = getParameterAsBigDecimal(DELTA_PARAMETER, new BigDecimal(0.0025));
	}

	@Override public String getType()
	{
		return m_strMoveType + "SellTrace";   
	}
	
	public void onOccurred(final BigDecimal nPrice, final Integer nRuleID)
	{
		m_strMoveType = (nPrice.compareTo(m_nPrice) > 0 ? "[^]" : "[v]");
		super.onOccurred(nPrice, null);
		m_nPrice = nPrice.add(m_nDelta);
	}
}


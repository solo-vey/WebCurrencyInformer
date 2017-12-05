package solo.model.stocks.item.rules.notify;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.RateInfo;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class EventSellTrace extends EventSell
{
	private static final long serialVersionUID = -5138333232638394248L;
	
	String m_strMoveType = StringUtils.EMPTY;
	protected BigDecimal m_nDelta;
		
	public EventSellTrace(final RateInfo oRateInfo, final String strPriceInfo)
	{
		super(oRateInfo, strPriceInfo);
		final String strDelta = CommonUtils.splitToPos(strPriceInfo, 1);
		m_nDelta = (StringUtils.isNotBlank(strDelta) ? MathUtils.fromString(strDelta).divide(new BigDecimal(100)) : new BigDecimal(0.0025));
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


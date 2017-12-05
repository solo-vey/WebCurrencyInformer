package solo.model.stocks.item.rules.notify;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.RateInfo;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class EventTrade extends EventBase
{
	private static final long serialVersionUID = -3592815874419984775L;
	
	protected BigDecimal m_nDelta;
	
	public EventTrade(final RateInfo oRateInfo, final String strPriceInfo)
	{
		super(oRateInfo, strPriceInfo);
		final String strDelta = CommonUtils.splitToPos(strPriceInfo, 1);
		m_nDelta = (StringUtils.isNotBlank(strDelta) ? MathUtils.fromString(strDelta).divide(new BigDecimal(100)) : new BigDecimal(0.0025));
	}

	@Override public String getType()
	{
		return "TRADE";   
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return getType() + "/" + m_oRateInfo.getCurrencyFrom() + "/" + 
			MathUtils.toCurrencyString(getMinTradePrice()) + "-" + MathUtils.toCurrencyString(getMaxTradePrice()) + 
			(null != nRuleID ? " /removeRule_" + nRuleID : StringUtils.EMPTY);   
	}
	
	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		final BigDecimal oTradePrice = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getTradesAnalysisResult().getAverageAllSumPrice();
		if (checkCurrentPrice(oTradePrice, getMinTradePrice(), getMaxTradePrice()))
			onOccurred(oTradePrice, nRuleID);
	}

	boolean checkCurrentPrice(final BigDecimal oTradePrice, final BigDecimal oTradeMinPrice, final BigDecimal oTradeMaxPrice)
	{
		return oTradePrice.compareTo(oTradeMinPrice) >= 0 && oTradePrice.compareTo(oTradeMaxPrice) <= 0;
	}

	BigDecimal getMaxTradePrice()
	{
		return new BigDecimal(m_nPrice.doubleValue() * (1 + m_nDelta.doubleValue()));
	}

	BigDecimal getMinTradePrice()
	{
		return new BigDecimal(m_nPrice.doubleValue() * (1 - m_nDelta.doubleValue()));
	}
}


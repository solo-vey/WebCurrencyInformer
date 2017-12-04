package solo.model.stocks.item.rules.notify;

import java.math.BigDecimal;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.RateInfo;

public class EventBuy extends EventBase
{
	public EventBuy(final RateInfo oRateInfo, final String strPriceInfo)
	{
		super(oRateInfo, strPriceInfo);
	}

	@Override public String getType()
	{
		return "BUY";   
	}
	
	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		final BigDecimal oAskPrice = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getAsksAnalysisResult().getAverageAllSumPrice();
		if (oAskPrice.compareTo(m_nPrice) <= 0)
			onOccurred(oAskPrice, nRuleID);
	}
}

package solo.model.stocks.item.rules.notify;

import java.math.BigDecimal;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.RateInfo;

public class EventSell extends EventBase
{
	public EventSell(final RateInfo oRateInfo, final String strPriceInfo)
	{
		super(oRateInfo, strPriceInfo);
	}

	@Override public String getType()
	{
		return "SELL";   
	}
	
	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		final BigDecimal oBidPrice = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getBidsAnalysisResult().getAverageAllSumPrice();
		if (oBidPrice.compareTo(m_nPrice) >= 0)
			onOccurred(oBidPrice, nRuleID);
	}
}


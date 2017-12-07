package solo.model.stocks.item.rules.notify;

import java.math.BigDecimal;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.RateInfo;

public class EventBuy extends EventBase
{
	private static final long serialVersionUID = 7603747872515498029L;

	public EventBuy(final RateInfo oRateInfo, final String strСommandLine)
	{
		super(oRateInfo, strСommandLine);
	}

	public EventBuy(final RateInfo oRateInfo, final String strСommandLine, final String strTemplate)
	{
		super(oRateInfo, strСommandLine, strTemplate);
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


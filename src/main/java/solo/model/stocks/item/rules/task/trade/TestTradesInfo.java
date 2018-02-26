package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.RateInfo;

public class TestTradesInfo extends TradesInfo implements ITest
{
	private static final long serialVersionUID = 2014380345686912919L;

	public TestTradesInfo(RateInfo oRateInfo, int nRuleID)
	{
		super(oRateInfo, nRuleID);
	}
	
	@Override public void tradeStart(final TaskTrade oTaskTrade)
	{
		setSum(getBuySum(), getTradeCount());
		
		m_nLockedSum = BigDecimal.ZERO;
		m_nSumToSell = BigDecimal.ZERO;
		m_nVolume = BigDecimal.ZERO;
		m_nLockedVolume = BigDecimal.ZERO;

		m_nReceivedSum = BigDecimal.ZERO;
		m_nSpendSum = BigDecimal.ZERO;
		
		m_nBuyVolume = BigDecimal.ZERO;
		m_nSoldVolume = BigDecimal.ZERO;
	}
	
	@Override public String getInfo()
	{
		return StringUtils.EMPTY;
		//return "[TEST] " + super.getInfo();
	}
}

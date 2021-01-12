package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.RateInfo;
import solo.utils.MathUtils;

public class TestTradesInfo extends TradesInfo implements ITest
{
	private static final long serialVersionUID = 2014380345686912919L;

	public TestTradesInfo(RateInfo oRateInfo, int nRuleID)
	{
		super(oRateInfo, nRuleID);
	}
		
	@Override public void tradeDone(final TaskTrade oTaskTrade)
	{
		super.tradeDone(oTaskTrade);
		
		final TradeInfo oLastTradeInfo = oTaskTrade.getTradeInfo();
		m_strCurrentState = MathUtils.toCurrencyStringEx3(oLastTradeInfo.getAveragedSoldPrice()) + " / " + 
							MathUtils.toCurrencyStringEx3(oLastTradeInfo.getAveragedBoughPrice()) + " / " + 
							MathUtils.toCurrencyStringEx3(oLastTradeInfo.getDelta());
		
		setSum(getBuySum(), 1);
		
		m_nLockedSum = BigDecimal.ZERO;
		m_nSumToSell = BigDecimal.ZERO;
		m_nVolume = BigDecimal.ZERO;
		m_nLockedVolume = BigDecimal.ZERO;

		m_nReceivedSum = BigDecimal.ZERO;
		m_nSpendSum = BigDecimal.ZERO;
		
		m_nBuyVolume = BigDecimal.ZERO;
		m_nSoldVolume = BigDecimal.ZERO;
	}
	
	@Override public String getCurrentState() 
	{	
		return StringUtils.EMPTY;
	}
	
	@Override public void setCurrentState(String strCurrentState) 
	{	
		/***/
	}
	
	@Override public String getInfo()
	{
		return (StringUtils.isNotBlank(m_strCurrentState) ? "<b>Last : " + m_strCurrentState + "</b>\r\n\r\n" : StringUtils.EMPTY)
				+ "Count: " + getTradeCount() + " [" + getRateInfo() + "]\r\n";
	}
}

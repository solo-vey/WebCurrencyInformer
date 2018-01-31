package solo.model.stocks.item.rules.task.trade;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.utils.MathUtils;

public class TradesInfo extends BaseObject implements Serializable
{
	private static final long serialVersionUID = -7601846839785106296L;
	
	protected RateInfo m_oRateInfo;
	protected Integer m_nRuleID;
	protected TradeHistory m_oHistory;
	protected int m_nTradeCount = 0;
	protected String m_strCurrentState = StringUtils.EMPTY;
	
	protected BigDecimal m_nSum = BigDecimal.ZERO;
	protected BigDecimal m_nLockedSum = BigDecimal.ZERO;
	protected BigDecimal m_nSumToSell = BigDecimal.ZERO;
	protected BigDecimal m_nVolume = BigDecimal.ZERO;
	protected BigDecimal m_nLockedVolume = BigDecimal.ZERO;
	protected BigDecimal m_nBuySum = BigDecimal.ZERO;
	protected BigDecimal m_nLossSum = BigDecimal.ZERO;

	protected BigDecimal m_nReceivedSum = BigDecimal.ZERO;
	protected BigDecimal m_nSpendSum = BigDecimal.ZERO;
	
	protected BigDecimal m_nBuyVolume = BigDecimal.ZERO;
	protected BigDecimal m_nSoldVolume = BigDecimal.ZERO;
	
	public TradesInfo(final RateInfo oRateInfo, final int nRuleID)
	{
		m_oRateInfo = oRateInfo;
		m_nRuleID = nRuleID;
	}
	
	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}
	
	public void setRateInfo(final RateInfo oRateInfo)
	{
		m_oRateInfo = oRateInfo;
	}

	public BigDecimal getDelta()
	{
		return m_nReceivedSum.add(m_nSpendSum.negate());
	}
	
	public Integer getRuleID()
	{
		return m_nRuleID;
	}
	
	public TradeHistory getHistory()
	{
		if (null == m_oHistory)
			m_oHistory = new TradeHistory(getRuleID(), "controler_" + m_oRateInfo);
		
		return m_oHistory;
	}
	
	public BigDecimal getSpendSum()
	{
		return m_nSpendSum;
	}
	
	public BigDecimal getReceivedSum()
	{
		return m_nReceivedSum;
	}
	
	public BigDecimal getSum()
	{
		return m_nSum;
	}
	
	public BigDecimal getLockedSum()
	{
		return m_nLockedSum;
	}
	
	public BigDecimal getFreeSum()
	{
		return m_nSum.add(m_nLockedSum.negate());
	}
	
	public BigDecimal getSumToSell()
	{
		return m_nSumToSell;
	}
	
	public void setSum(final BigDecimal nSum, final Integer nMaxTrades)
	{
		m_nSum = nSum;
		m_nBuySum = MathUtils.getRoundedBigDecimal(nSum.doubleValue() / nMaxTrades, TradeUtils.getVolumePrecision(m_oRateInfo)); 
	}
	
	public BigDecimal getBuySum()
	{
		return m_nBuySum;
	}
	
	public BigDecimal getLossSum()
	{
		if (null == m_nLossSum)
			m_nLossSum = BigDecimal.ZERO;
		return m_nLossSum;
	}
	
	public BigDecimal getSoldVolume()
	{
		return m_nSoldVolume;
	}
	
	public BigDecimal getBuyVolume()
	{
		return m_nBuyVolume;
	}
	
	public BigDecimal getVolume()
	{
		return m_nVolume;
	}
	
	public BigDecimal getLockedVolume()
	{
		return m_nLockedVolume;
	}
	
	public BigDecimal getFreeVolume()
	{
		return m_nVolume.add(m_nLockedVolume.negate());
	}
	
	public Integer getTradeCount()
	{
		return m_nTradeCount;
	}
	
	public String getCurrentState()
	{
		if (StringUtils.isBlank(m_strCurrentState))
			return StringUtils.EMPTY;
		
		return "State [" + m_strCurrentState + "]";
	}
	
	public void addBuy(BigDecimal nSpendSum, BigDecimal nBuyVolume)
	{
		if (nSpendSum.compareTo(BigDecimal.ZERO) == 0 && nBuyVolume.compareTo(BigDecimal.ZERO) == 0)
			return;
		
		m_nSpendSum = m_nSpendSum.add(nSpendSum);
		m_nSum = m_nSum.add(nSpendSum.negate());

		m_nBuyVolume = m_nBuyVolume.add(nBuyVolume);
		m_nVolume = m_nVolume.add(nBuyVolume);
	}
	
	public void addSell(BigDecimal nReceivedSum, BigDecimal nSoldVolume)
	{
		if (nReceivedSum.compareTo(BigDecimal.ZERO) == 0 && nSoldVolume.compareTo(BigDecimal.ZERO) == 0)
			return;

		m_nReceivedSum = m_nReceivedSum.add(nReceivedSum);
		m_nSum = m_nSum.add(nReceivedSum);

		m_nSoldVolume = m_nSoldVolume.add(nSoldVolume);
		m_nVolume = m_nVolume.add(nSoldVolume.negate());
	}
	
	public void setLossSum(final BigDecimal nLossSum)
	{
		m_nLossSum = nLossSum;
		
		addToHistory("Set loss sum : " + MathUtils.toCurrencyString(nLossSum)); 
	}
	
	public void tradeStart(final TaskTrade oTaskTrade)
	{
	}
	
	public void buyDone(final TaskTrade oTaskTrade)
	{
	}
	
	public void tradeDone(final TaskTrade oTaskTrade)
	{
		m_nTradeCount++;
		final BigDecimal nTradeDelta = oTaskTrade.getTradeInfo().getFullDelta();
		if (nTradeDelta.compareTo(BigDecimal.ZERO) < 0)
			setLossSum(nTradeDelta.negate());
		
		addToHistory(oTaskTrade.getTradeInfo().getInfo()); 
	}
	
	protected void addToHistory(final String strMessage)
	{
		final DateFormat oDateFormat = new SimpleDateFormat("dd.MM HH:mm:ss");
		getHistory().addToHistory(oDateFormat.format(new Date()) + " " + strMessage);
	}
	
	public void setCurrentState(final String strCurrentState)
	{
		m_strCurrentState = strCurrentState;
	}
	
	public void updateOrderInfo(final List<ITradeTask> aTaskTrades)
	{
		m_nLockedSum = BigDecimal.ZERO;
		m_nLockedVolume = BigDecimal.ZERO;
		m_nSumToSell = BigDecimal.ZERO;
		for(final ITradeTask oTaskTrade : aTaskTrades)
		{
			final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
			if (oOrder.isNull() || oOrder.isError() || oOrder.isCanceled() || oOrder.isDone())
				continue;
			
			if (oOrder.getSide().equals(OrderSide.BUY))
				m_nLockedSum = m_nLockedSum.add(oOrder.getSum());

			if (oOrder.getSide().equals(OrderSide.SELL))
			{
				m_nLockedVolume = m_nLockedVolume.add(oOrder.getVolume());
				m_nSumToSell = m_nSumToSell.add(oOrder.getSum());
			}
		}
	}
	
	public String getInfo()
	{
		final BigDecimal nReceiveAndSellSum = getReceivedSum().add(getSumToSell());
		final BigDecimal nDelta = nReceiveAndSellSum.add(getSpendSum().negate());
		return  "Count: " + getTradeCount() + " [" + getRateInfo().toString() + "]\r\n" + 
				"Money: " + MathUtils.toCurrencyStringEx3(getSum()) + "/" + MathUtils.toCurrencyStringEx3(getLockedSum()) + "/" + MathUtils.toCurrencyStringEx3(getFreeSum()) + "/" + MathUtils.toCurrencyStringEx3(getSumToSell()) + "\r\n" + 
				"Volume:" + MathUtils.toCurrencyStringEx2(getVolume()) + "/" + MathUtils.toCurrencyStringEx2(getLockedVolume()) +  "/" + MathUtils.toCurrencyStringEx2(getFreeVolume()) + "\r\n" + 
				"Trades: " + MathUtils.toCurrencyStringEx3(nReceiveAndSellSum) + "-" + MathUtils.toCurrencyStringEx3(getSpendSum()) + "=" + MathUtils.toCurrencyStringEx3(nDelta) + "\r\n" + 
				getCurrentState();
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		String strResult = StringUtils.EMPTY;
		
		if (getSum().compareTo(BigDecimal.ZERO) > 0)
			strResult += "Sum: " + MathUtils.toCurrencyStringEx3(getSum()) + "\r\n";
		if (getLockedSum().compareTo(BigDecimal.ZERO) > 0)
			strResult += "LockedSum: " + MathUtils.toCurrencyStringEx3(getLockedSum()) + "\r\n";
		if (getSumToSell().compareTo(BigDecimal.ZERO) > 0)
			strResult += "SumToSell: " + MathUtils.toCurrencyStringEx3(getSumToSell()) + "\r\n";
		if (getVolume().compareTo(BigDecimal.ZERO) > 0)
			strResult += "Volume: " + MathUtils.toCurrencyStringEx2(getVolume()) + "\r\n";
		if (getLockedVolume().compareTo(BigDecimal.ZERO) > 0)
			strResult += "LockedVolume: " + MathUtils.toCurrencyStringEx2(getLockedVolume()) + "\r\n";
		if (getBuySum().compareTo(BigDecimal.ZERO) > 0)
			strResult += "BuySum: " + MathUtils.toCurrencyStringEx3(getBuySum()) + "\r\n";
		if (getLossSum().compareTo(BigDecimal.ZERO) > 0)
			strResult += "LossSum:" + MathUtils.toCurrencyStringEx3(getLossSum()) + "\r\n";
		
		return strResult;
	}
}

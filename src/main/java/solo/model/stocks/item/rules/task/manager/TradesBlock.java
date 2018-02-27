package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.math.BigDecimal;

import solo.model.stocks.item.rules.task.trade.TradeInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.utils.MathUtils;

public class TradesBlock implements Serializable
{
	private static final long serialVersionUID = -4127051943452696486L;
	
	protected int m_nCount = 0;
	protected BigDecimal m_nSpendSum = BigDecimal.ZERO;
	protected BigDecimal m_nReceivedSum = BigDecimal.ZERO;

	public int getCount()
	{
		return m_nCount;
	}
	
	public BigDecimal getSpendSum()
	{
		return m_nSpendSum;
	}
	
	public BigDecimal getReceivedSum()
	{
		return m_nReceivedSum;
	}
	
	public BigDecimal getTotalSum()
	{
		return m_nSpendSum.add(m_nReceivedSum);
	}

	public void addTrade(final TradeInfo oTradeInfo)
	{
		m_nCount++;
		m_nSpendSum = m_nSpendSum.add(oTradeInfo.getSpendSum());
		m_nReceivedSum = m_nReceivedSum.add(oTradeInfo.getReceivedSum());
		
		final BigDecimal nNeedSellVolume = oTradeInfo.getNeedSellVolume();
		if (nNeedSellVolume.compareTo(BigDecimal.ZERO) > 0)
		{
			final BigDecimal nNeedSellSum = nNeedSellVolume.multiply(oTradeInfo.getAveragedBoughPrice());
			m_nSpendSum = m_nSpendSum.add(nNeedSellSum.negate());
		}
	}
	
	public BigDecimal getDelta()
	{
		return getReceivedSum().add(getSpendSum().negate());
	}
	
	public BigDecimal getPercent()
	{
		final BigDecimal nAveregeTradeSum = MathUtils.getBigDecimal(getSpendSum().doubleValue() / m_nCount, TradeUtils.DEFAULT_PRICE_PRECISION);
		return MathUtils.getBigDecimal(getDelta().doubleValue() / nAveregeTradeSum.doubleValue() * 100, 2);
	}
	
	public void addTrade(final TradesBlock oTradesBlock)
	{
		m_nCount += oTradesBlock.getCount();
		m_nSpendSum = m_nSpendSum.add(oTradesBlock.getSpendSum());
		m_nReceivedSum = m_nReceivedSum.add(oTradesBlock.getReceivedSum());
	}
	
	@Override public String toString()
	{
		return m_nCount + " / " + MathUtils.toCurrencyStringEx3(getReceivedSum().add(getSpendSum())) + " / " + 
						MathUtils.toCurrencyStringEx3(getDelta()) + " / " + MathUtils.toCurrencyStringEx3(getPercent());
	}
}

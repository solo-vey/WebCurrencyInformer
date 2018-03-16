package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.rules.task.trade.TradeInfo;
import solo.utils.MathUtils;

public class TradesBlock implements Serializable
{
	public static final String TYPE_FULL = "full";
	public static final String TYPE_SHORT = "short";
	public static final String TYPE_ONLY_PERCENT = "only_percent";

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
		if (getSpendSum().compareTo(BigDecimal.ZERO) == 0 || getReceivedSum().compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;
		
		final double nAveregeTradeSum = getSpendSum().doubleValue() / m_nCount;
		return MathUtils.getBigDecimal(getDelta().doubleValue() / nAveregeTradeSum * 100, 2);
	}
	
	public void addTrade(final TradesBlock oTradesBlock)
	{
		m_nCount += oTradesBlock.getCount();
		m_nSpendSum = m_nSpendSum.add(oTradesBlock.getSpendSum());
		m_nReceivedSum = m_nReceivedSum.add(oTradesBlock.getReceivedSum());
	}
	
	public String asString(final String strType)
	{
		final boolean bIsLostMoney = (getPercent().compareTo(BigDecimal.ZERO) < 0);
		final String strStyle = (bIsLostMoney ? "<code>" : StringUtils.EMPTY);
		final String strCloseStyle = (bIsLostMoney ? "</code>" : StringUtils.EMPTY);
		
		return strStyle + 
						(!strType.equalsIgnoreCase("only_percent") ? m_nCount : StringUtils.EMPTY) + 
						(strType.equalsIgnoreCase(TYPE_FULL) ? " / " + MathUtils.toCurrencyStringEx3(getReceivedSum().add(getSpendSum())) : StringUtils.EMPTY) + 
						(strType.equalsIgnoreCase(TYPE_FULL) ? " / " + MathUtils.toCurrencyStringEx3(getDelta()) : StringUtils.EMPTY) + 
						"[" + MathUtils.toCurrencyStringEx3(getPercent()) + "%]" + strCloseStyle;
	}
	
	@Override public String toString()
	{
		return asString(TYPE_SHORT);
	}
}

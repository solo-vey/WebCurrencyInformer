package solo.model.stocks.item.rules.task.money;

import java.io.Serializable;
import java.math.BigDecimal;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.RateInfo;

public class TradeMoney extends BaseObject implements Serializable
{
	private static final long serialVersionUID = 5977672129252262127L;
	
	protected BigDecimal m_nVolume = BigDecimal.ZERO;
	protected BigDecimal m_nSum = BigDecimal.ZERO;
	protected Integer m_nTradeID = -1;
	protected RateInfo m_oRateInfo;

	public TradeMoney(final BigDecimal nVolume, final BigDecimal nSum, final RateInfo oRateInfo)
	{
		m_nVolume = nVolume;
		m_nSum = nSum;
		m_oRateInfo = oRateInfo;
	}
	
	public BigDecimal getVolume()
	{
		return m_nVolume;
	}
	
	public BigDecimal getSum()
	{
		return m_nSum;
	}
	
	public void addVolume(final BigDecimal nChangeVolume)
	{
		m_nVolume = m_nVolume.add(nChangeVolume);
	}
	
	public void addSum(final BigDecimal nChangeSum)
	{
		m_nSum = m_nSum.add(nChangeSum);
	}
	
	public Integer getTradeID()
	{
		return m_nTradeID;
	}
	
	public void setTradeID(final Integer nTradeID)
	{
		m_nTradeID = nTradeID;
	}
	
	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}
}

package solo.model.stocks.item;

import java.io.Serializable;
import java.math.BigDecimal;

import solo.utils.JsonUtils;

public class RateParamters implements Serializable
{
	private static final long serialVersionUID = -7207375588298563812L;
	
	protected BigDecimal m_nMinQuantity = BigDecimal.ZERO;
	protected BigDecimal m_nMaxQuantity = BigDecimal.ZERO;
	protected BigDecimal m_nMinPrice = BigDecimal.ZERO;
	protected BigDecimal m_nMaxPrice = BigDecimal.ZERO;
	protected BigDecimal m_nMinAmount = BigDecimal.ZERO;
	protected BigDecimal m_nMaxAmount = BigDecimal.ZERO;
	protected BigDecimal m_nVolume = BigDecimal.ZERO;
	protected BigDecimal m_nTradeFee = BigDecimal.ZERO;

	
	public BigDecimal getMinQuantity()
	{
		return m_nMinQuantity;
	}
	
	public BigDecimal getMaxQuantity()
	{
		return m_nMaxQuantity;
	}
	
	public BigDecimal getMinPrice()
	{
		return m_nMinPrice;
	}
	
	public BigDecimal getMaxPrice()
	{
		return m_nMaxPrice;
	}	
	public BigDecimal getMinAmount()
	{
		return m_nMinAmount;
	}
	
	public BigDecimal getMaxAmount()
	{
		return m_nMaxAmount;
	}
	
	public BigDecimal getVolume()
	{
		return m_nVolume;
	}
	
	public BigDecimal getTradeFee()
	{
		return m_nTradeFee;
	}
	
	public void setMinQuantity(final BigDecimal nMinQuantity)
	{
		m_nMinQuantity = nMinQuantity;
	}
	
	public void setMaxQuantity(final BigDecimal nMaxQuantity)
	{
		m_nMaxQuantity = nMaxQuantity;
	}
	
	public void setMinPrice(final BigDecimal nMinPrice)
	{
		m_nMinPrice = nMinPrice;
	}
	
	public void setMaxPrice(final BigDecimal nMaxPrice)
	{
		m_nMaxPrice = nMaxPrice;
	}
	
	public void setMinAmount(final BigDecimal nMinAmount)
	{
		m_nMinAmount = nMinAmount;
	}
	
	public void setMaxAmount(final BigDecimal nMaxAmount)
	{
		m_nMaxAmount = nMaxAmount;
	}	
	
	public void setVolume(final BigDecimal nVolume)
	{
		m_nVolume = nVolume;
	}
	
	public void setTradeFee(final BigDecimal nTradeFee)
	{
		m_nTradeFee = nTradeFee;
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		return JsonUtils.toJson(this);
	}
}

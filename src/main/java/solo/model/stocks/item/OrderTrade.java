package solo.model.stocks.item;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class OrderTrade implements Serializable
{
	private static final long serialVersionUID = -1693366150779746746L;
	
	protected String m_strID = StringUtils.EMPTY;
	protected OrderSide m_oSide = OrderSide.BUY;
	protected Date m_oCreated = new Date();
	
	protected BigDecimal m_nPrice = BigDecimal.ZERO;
	protected BigDecimal m_nVolume = BigDecimal.ZERO;
	protected BigDecimal m_nSum = BigDecimal.ZERO;

	public OrderTrade()
	{
	}
	
	public String getId()
	{
		return (null != m_strID ? m_strID : StringUtils.EMPTY);
	}
	
	public void setId(final String strID)
	{
		m_strID = strID.toLowerCase();
	}
	
	public OrderSide getSide()
	{
		return m_oSide;
	}
	
	public void setSide(final OrderSide oSide)
	{
		m_oSide = oSide;
	}
	
	public void setSide(final String strSide)
	{
		if (strSide.equalsIgnoreCase(OrderSide.SELL.toString()))
			m_oSide = OrderSide.SELL;
		else
		if (strSide.equalsIgnoreCase("ask"))
			m_oSide = OrderSide.SELL;
		else
			m_oSide = OrderSide.BUY;
	}
	
	public BigDecimal getPrice()
	{
		return m_nPrice;
	}
	
	public void setPrice(final BigDecimal nPrice)
	{
		m_nPrice = nPrice;
	}
	
	public BigDecimal getVolume()
	{
		return m_nVolume;
	}
	
	public void setVolume(final BigDecimal nVolume)
	{
		m_nVolume = nVolume;
	}
	
	public BigDecimal getSum()
	{
		return m_nSum;
	}
	
	public void setSum(final BigDecimal nSum)
	{
		m_nSum = nSum;
	}
	
	public Date getCreated()
	{
		return m_oCreated;
	}
	
	public void setCreated(final Date oCreated)
	{
		m_oCreated = oCreated;
	}
	
	public void setCreated(final String strCreated, final String strFormat)
	{
		try
		{
			final SimpleDateFormat oFormatter = new SimpleDateFormat(strFormat);
			setCreated(oFormatter.parse(strCreated));
		}
		catch (ParseException e) 
		{ 
			System.err.println("Error parsing date [" + strCreated + "]");
		}		
	}
	
	@Override public String toString()
	{
		return getSide() + "/" + getId() + "/" + getVolume() + "/" + getPrice() + "/" + getSum();
	}
	
	@Override public boolean equals(final Object obj)
	{
		if (null == obj)
			return false;
		
		if (!(obj instanceof OrderTrade))
			return false;
		
		final String strCheckID = ((OrderTrade)obj).getId();
		return getId().equals(strCheckID);
	}
}

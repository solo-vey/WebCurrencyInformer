package solo.model.stocks;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Order extends BaseObject
{
	protected String m_strID;
	protected BigDecimal m_nPrice;
	protected String m_strState;
	protected BigDecimal m_nVolume;
	protected Date m_oCreated;
	
	public String getId()
	{
		return m_strID;
	}
	
	public void setId(final String strID)
	{
		m_strID = strID;
	}
	
	public BigDecimal getPrice()
	{
		return m_nPrice;
	}
	
	public void setPrice(final BigDecimal nPrice)
	{
		m_nPrice = nPrice;
	}
	
	public String getState()
	{
		return m_strState;
	}
	
	public void setState(final String strState)
	{
		m_strState = strState;
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
		return m_nPrice.multiply(m_nVolume);
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
		catch (ParseException e) { }		
	}
}


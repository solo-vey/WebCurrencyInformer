package solo.model.stocks.item.analyse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import solo.model.stocks.BaseObject;

public class JapanCandle extends BaseObject implements Serializable
{
	private static final long serialVersionUID = 1817125412807203139L;
	
	protected BigDecimal m_nMin = BigDecimal.ZERO;
	protected BigDecimal m_nMax = BigDecimal.ZERO;
	protected BigDecimal m_nStart = BigDecimal.ZERO;
	protected BigDecimal m_nEnd = BigDecimal.ZERO;
	protected Date m_oDate = new Date();
	
	public Date getDate()
	{
		return m_oDate;
	}

	public BigDecimal getMin()
	{
		return m_nMin;
	}
	
	public BigDecimal getMax()
	{
		return m_nMax;
	}
	
	public BigDecimal getStart()
	{
		return m_nStart;
	}
	
	public BigDecimal getEnd()
	{
		return m_nEnd;
	}
	
	public void setValue(final BigDecimal nValue)
	{
		if (m_nStart.equals(BigDecimal.ZERO))
			m_nStart = nValue;
		m_nEnd = nValue;
	
		if (m_nMin.equals(BigDecimal.ZERO) || nValue.compareTo(m_nMin) < 0)
			m_nMin = nValue;

		if (m_nMax.equals(BigDecimal.ZERO) || nValue.compareTo(m_nMax) > 0)
			m_nMax = nValue;
	}
	
	public String getType()
	{
		return (m_nStart.compareTo(m_nEnd) > 0 ? "v" : m_nStart.compareTo(m_nEnd) < 0 ? "^" : "-");  
	}
}

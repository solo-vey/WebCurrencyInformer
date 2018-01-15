package solo.model.stocks.item.analyse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import solo.model.stocks.BaseObject;
import solo.utils.MathUtils;

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
	
	public CandleType getCandleType()
	{
		final BigDecimal nVerySmall = new BigDecimal(0.1);
		final BigDecimal nSmall = new BigDecimal(0.35);
		final BigDecimal nVerySmallLength = MathUtils.getBigDecimal(m_nMax.doubleValue() / 100 / 10, 8);
		
		final BigDecimal nLength = m_nMax.add(m_nMin.negate()); 
		final BigDecimal nBodyMax = (m_nStart.compareTo(m_nEnd) > 0 ? m_nStart : m_nEnd);
		final BigDecimal nBodyMin = (m_nStart.compareTo(m_nEnd) > 0 ? m_nEnd : m_nStart);
		final BigDecimal nBodyLength = nBodyMax.add(nBodyMin.negate());
		final boolean bIsGrow = (m_nStart.compareTo(m_nEnd) < 0);
		final BigDecimal nTopShadowLength = m_nMax.add(nBodyMax.negate()); 
		final BigDecimal nBottomShadowLength = nBodyMin.add(m_nMin.negate()); 
		if (nLength.compareTo(BigDecimal.ZERO) == 0)
			return CandleType.DOJI_GROWTH;

		if (nLength.compareTo(nVerySmallLength) < 0)
			return (bIsGrow ? CandleType.DOJI_GROWTH : CandleType.DOJI_FALL);
			
		final BigDecimal nBodyPercent = MathUtils.getBigDecimal(nBodyLength.doubleValue() / nLength.doubleValue(), 8);
		final BigDecimal nTopShadowPercent = MathUtils.getBigDecimal(nTopShadowLength.doubleValue() / nLength.doubleValue(), 8);
		final BigDecimal nBottomShadowPercent = MathUtils.getBigDecimal(nBottomShadowLength.doubleValue() / nLength.doubleValue(), 8);
		
		if (nTopShadowPercent.compareTo(nVerySmall) <= 0 && nBodyPercent.compareTo(nSmall) <= 0)
			return (bIsGrow ? CandleType.HANGING_MAN_GROWTH : CandleType.HAMMER_FALL);

		if (nBottomShadowPercent.compareTo(nVerySmall) <= 0 && nBodyPercent.compareTo(nSmall) <= 0)
			return (bIsGrow ? CandleType.HAMMER_GROWTH : CandleType.HANGING_MAN_FALL);

 		if (nTopShadowPercent.compareTo(BigDecimal.ZERO) == 0 && nBottomShadowPercent.compareTo(BigDecimal.ZERO) == 0)
			return (bIsGrow ? CandleType.MARUBOZU_GROWTH : CandleType.MARUBOZU_FALL);

		if (nBodyPercent.compareTo(nVerySmall) <= 0)
			return (bIsGrow ? CandleType.DOJI_GROWTH : CandleType.DOJI_FALL);
			
		if (nBodyPercent.compareTo(nVerySmall) > 0 && nBodyPercent.compareTo(nSmall) <= 0)
			return (bIsGrow ? CandleType.LONG_SHADOW_GROWTH : CandleType.LONG_SHADOW_FALL);
			
		return (bIsGrow ? CandleType.LONG_BODY_GROWTH : CandleType.LONG_BODY_FALL); 
	}
}

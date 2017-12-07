package solo.model.stocks.item.command.base;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.stocks.BaseObject;
import solo.model.stocks.item.RateInfo;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

abstract public class HasParameters extends BaseObject
{
	final static public String TAIL_PARAMETER = "...";

	final protected Map<String, String> m_oParameters;
	final protected String m_strTemplate;
	final protected String m_strCommandInfo;

	public HasParameters()
	{
		this(StringUtils.EMPTY, StringUtils.EMPTY);
	}
	
	public HasParameters(final String strParameters, final String strTemplate)
	{
		m_strCommandInfo = strParameters;
		m_strTemplate = strTemplate;
		m_oParameters = CommonUtils.splitParameters(strParameters, getTemplate());
	}
	
	public String getCommandLine()
	{
		return m_strCommandInfo;
	}
	
	public String getTemplate()
	{
		return m_strTemplate;
	}
	
	public String getParameter(final String strParameterName)
	{
		final String strKey = strParameterName.toLowerCase().replace("#", StringUtils.EMPTY).trim();
		return (m_oParameters.containsKey(strKey) ? m_oParameters.get(strKey) : StringUtils.EMPTY);
	}
	
	public BigDecimal getParameterAsBigDecimal(final String strParameterName)
	{
		return getParameterAsBigDecimal(strParameterName, BigDecimal.ZERO);
	}
	
	public BigDecimal getParameterAsBigDecimal(final String strParameterName, final BigDecimal nDefault)
	{
		final String strValue = getParameter(strParameterName);
		if (StringUtils.isNotBlank(strValue))
			return MathUtils.fromString(strValue);
		
		return nDefault;
	}
	
	public Integer getParameterAsInt(final String strParameterName)
	{
		final String strValue = getParameter(strParameterName);
		if (StringUtils.isNotBlank(strValue))
			return Integer.valueOf(strValue);
		
		return Integer.MIN_VALUE;
	}
	
	public RateInfo getParameterAsRateInfo(final String strParameterName)
	{
		final String strValue = getParameter(strParameterName);
		if (StringUtils.isBlank(strValue))
			return null;

		final String strCurrencyFrom = strValue.toUpperCase();
		return new RateInfo(Currency.valueOf(strCurrencyFrom), Currency.UAH); 
	}
	
	public Object getParameterAsEnum(final String strParameterName, final Class<?> oEnum)
	{
		try
		{
			oEnum.getEnumConstants();
			final String strValue = getParameter(strParameterName);
			for(final Object oEnumValue : oEnum.getEnumConstants())
			{
				if (oEnumValue.toString().equalsIgnoreCase(strValue))
					return oEnumValue;
			}
		}
		catch (final Exception e) {}
		
		return null;
	}
}

package solo.model.stocks.item.command.base;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.stocks.BaseObject;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.item.rules.task.strategy.trade.ITradeStrategy;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

abstract public class HasParameters extends BaseObject
{
	public static final String TAIL_PARAMETER = "...";

	protected final Map<String, String> m_oParameters;
	protected final String m_strTemplate;
	protected final String m_strCommandInfo;

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
	
	public void setParameter(final String strParameterName, final String strValue)
	{
		final String strKey = strParameterName.toLowerCase().replace("#", StringUtils.EMPTY).trim();
		m_oParameters.put(strKey, strValue);
	}

	public Boolean getParameterAsBoolean(final String strParameterName)
	{
		return getParameter(strParameterName).equalsIgnoreCase("true");
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
	
	public Integer getParameterAsInt(final String strParameterName, final Integer nDefault)
	{
		final String strValue = getParameter(strParameterName);
		if (StringUtils.isNotBlank(strValue))
			return Integer.valueOf(strValue);
		
		return nDefault;
	}
	
	public ITradeStrategy getParameterAsTradeStrategy(final String strParameterName, final ITradeStrategy oDefaultTradeStrategy)
	{
		final String strValue = getParameter(strParameterName);
		if (StringUtils.isNotBlank(strValue))
			return StrategyFactory.getTradeStrategy(strValue);
		
		return oDefaultTradeStrategy;
	}

	public RateInfo getParameterAsRateInfo(final String strParameterName)
	{
		final String strValue = getParameter(strParameterName);
		if (StringUtils.isBlank(strValue))
			return null;

		Currency oCurrencyFrom = Currency.UAH;
		for(final Currency oCurrency : Currency.values())
		{
			if (!strValue.toUpperCase().startsWith(oCurrency.toString().toUpperCase()))
				continue;
			
			oCurrencyFrom = oCurrency;
			final String strCurrencyTo = strValue.toUpperCase().substring(oCurrencyFrom.toString().toUpperCase().length());
			final Currency oCurrencyTo = (StringUtils.isNotBlank(strCurrencyTo) ? Currency.valueOf(strCurrencyTo.toUpperCase()) : Currency.UAH);
			
			if (null != oCurrencyTo)
			{
				final RateInfo oRateInfo = new RateInfo(oCurrencyFrom, oCurrencyTo);
				if (WorkerFactory.getStockSource().getAllRates().contains(oRateInfo))
					return oRateInfo;
			
				return new RateInfo(oCurrencyFrom, oCurrencyTo, true); 
			}
		}

		return null;
	}
	
	public Date getParameterAsDate(final String strParameterName)
	{
		final String strValue = getParameter(strParameterName);
		if (StringUtils.isBlank(strValue))
			return null;

		try
		{
			final DateFormat oFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
			return oFormat.parse(strValue);
		}
		catch (ParseException e) {/***/}
		return null;
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
		catch (final Exception e) {/***/}
		
		return null;
	}
}

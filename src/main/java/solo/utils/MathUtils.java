package solo.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;

public class MathUtils
{
	private static final String DECIMAL_FORMAT = "#.########";

	MathUtils() 
	{
		throw new IllegalStateException("Utility class");
	}
	
	public static BigDecimal getBigDecimal(final BigDecimal nValue, final int nScale)
	{
		return getBigDecimal(nValue.doubleValue(), nScale);
	}
	
	public static BigDecimal getBigDecimal(final double nValue, final int nScale)
	{
		final BigDecimal oOriginalValue = BigDecimal.valueOf(nValue);
		return oOriginalValue.setScale(nScale, RoundingMode.DOWN);
	}

	public static BigDecimal getRoundedBigDecimal(final double nValue, final int nScale)
	{
		final BigDecimal oOriginalValue = BigDecimal.valueOf(nValue);
		return oOriginalValue.setScale(nScale, RoundingMode.CEILING);
	}
	
	public static BigDecimal fromString(final String strValue)
	{
		return BigDecimal.valueOf(Double.valueOf(strValue));
	}
	
	public static String toCurrencyString(final BigDecimal oValue)
	{
		if (null == oValue)
			return "NaN";

		final DecimalFormat oDecimalFormat = new DecimalFormat("#,###.########");
		return oDecimalFormat.format(oValue).replace(",", ".").replace((char)0xA0, ',').trim();
	}
	
	public static String toPercentString(final BigDecimal oValue)
	{
		if (null == oValue)
			return "NaN";

		final DecimalFormat oDecimalFormat = new DecimalFormat("#,###.##");
		return oDecimalFormat.format(oValue).replace(",", ".").replace((char)0xA0, ',').trim();
	}
	
	public static String toCurrencyStringEx3(final BigDecimal oValue)
	{
		if (null == oValue)
			return "NaN";
		
		if (oValue.doubleValue() >= 10 || oValue.doubleValue() <= -10)
		{
			final DecimalFormat oDecimalFormat = new DecimalFormat("#,###.##");
			return oDecimalFormat.format(oValue).replace(",", ".").replace((char)0xA0, ',').trim();
		}

		return toCurrencyStringEx2(oValue);
	}
	
	public static String toCurrencyStringEx2(final BigDecimal oValue)
	{
		if (null == oValue)
			return "NaN";

		if (oValue.doubleValue() >= 10 || oValue.doubleValue() <= -10)
			return toCurrencyString(oValue);

		if (oValue.compareTo(BigDecimal.ZERO) == 0)
			return "0";
		
		final String strPrefix = (oValue.compareTo(BigDecimal.ZERO) < 0 ? "-" : StringUtils.EMPTY);
		final BigDecimal nPositiveValue = (oValue.compareTo(BigDecimal.ZERO) >= 0 ? oValue : oValue.negate());
		if (nPositiveValue.doubleValue() > 0.001)
		{
			final DecimalFormat oDecimalFormat = new DecimalFormat(DECIMAL_FORMAT);
			return strPrefix + oDecimalFormat.format(nPositiveValue.doubleValue()).replace(",", ".").trim();
		}

		if (nPositiveValue.doubleValue() > 0.000001)
		{
			final DecimalFormat oDecimalFormat = new DecimalFormat(DECIMAL_FORMAT);
			return strPrefix + oDecimalFormat.format(nPositiveValue.doubleValue() * 1000).replace(",", ".").trim() + "(-3)";
		}

		final DecimalFormat oDecimalFormat = new DecimalFormat(DECIMAL_FORMAT);
		return strPrefix + oDecimalFormat.format(nPositiveValue.doubleValue() * 1000000).replace(",", ".").trim() + "(-6)";
	}
}

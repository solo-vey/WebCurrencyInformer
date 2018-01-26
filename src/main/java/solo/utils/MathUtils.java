package solo.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.apache.http.annotation.Obsolete;

public class MathUtils
{
	public static BigDecimal getBigDecimal(final double nValue, final int nScale)
	{
		final BigDecimal oOriginalValue = new BigDecimal(nValue);
		return oOriginalValue.setScale(nScale, RoundingMode.DOWN);
	}

	@Obsolete
	public static BigDecimal getBigDecimalRoundedUp(final double nValue, final int nScale)
	{
		final BigDecimal oOriginalValue = new BigDecimal(nValue);
		return oOriginalValue.setScale(nScale, RoundingMode.UP);
	}

	public static BigDecimal getRoundedBigDecimal(final double nValue, final int nScale)
	{
		final BigDecimal oOriginalValue = new BigDecimal(nValue);
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
		return oDecimalFormat.format(oValue).replace(",", ".").replace((char)0xA0, (char)0x20).trim();
	}
	
	@Obsolete
	public static String toCurrencyStringEx3(final BigDecimal oValue)
	{
		if (null == oValue)
			return "NaN";
		
		if (oValue.doubleValue() >= 10 || oValue.doubleValue() <= 0)
		{
			final DecimalFormat oDecimalFormat = new DecimalFormat("#,###.##");
			return oDecimalFormat.format(oValue).replace(",", ".").replace((char)0xA0, (char)0x20).trim();
		}

		return toCurrencyStringEx2(oValue);
	}
	
	public static String toCurrencyStringEx2(final BigDecimal oValue)
	{
		if (null == oValue)
			return "NaN";

		if (oValue.doubleValue() >= 10 || oValue.doubleValue() <= 0)
			return toCurrencyString(oValue);

		if (oValue.compareTo(BigDecimal.ZERO) == 0)
			return "0";
		
		if (oValue.doubleValue() > 0.001)
		{
			final DecimalFormat oDecimalFormat = new DecimalFormat("#.########");
			return oDecimalFormat.format(oValue.doubleValue()).replace(",", ".").trim();
		}

		if (oValue.doubleValue() > 0.000001)
		{
			final DecimalFormat oDecimalFormat = new DecimalFormat("#.########");
			return oDecimalFormat.format(oValue.doubleValue() * 1000).replace(",", ".").trim() + "(-3)";
		}

		final DecimalFormat oDecimalFormat = new DecimalFormat("#.########");
		return oDecimalFormat.format(oValue.doubleValue() * 1000000).replace(",", ".").trim() + "(-6)";
	}
}

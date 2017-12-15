package solo.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

public class MathUtils
{
	public static BigDecimal getBigDecimal(final double nValue, final int nScale)
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

		if (oValue.doubleValue() >= 0)
			return NumberFormat.getCurrencyInstance(Locale.US).format(oValue).replace("$", StringUtils.EMPTY).trim();
		else
			return "-" + NumberFormat.getCurrencyInstance(Locale.US).format(oValue).replace("$", StringUtils.EMPTY).trim();
	}
	
	public static String toCurrencyStringEx(final BigDecimal oValue)
	{
		if (null == oValue)
			return "NaN";

		if (oValue.doubleValue() > 1)
			return toCurrencyString(oValue);
		
		final DecimalFormat oDecimalFormat = new DecimalFormat("#.00000000");
		return "0" + oDecimalFormat.format(oValue.doubleValue()).replace(",", ".").trim();
	}
}

package solo.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import solo.model.currency.Currency;

public class MathUtils
{
	public static BigDecimal getBigDecimal(final double nValue, final int nScale)
	{
		final BigDecimal oOriginalValue = new BigDecimal(nValue);
		return oOriginalValue.setScale(nScale, RoundingMode.CEILING);
	}
	
	public static String toCurrencyString(final BigDecimal oValue, final Currency oCurrency)
	{
		return DecimalFormat.getCurrencyInstance(oCurrency.getLocale()).format(oValue);
	}
}

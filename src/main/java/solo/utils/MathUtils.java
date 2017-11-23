package solo.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils
{
	public static BigDecimal getBigDecimal(final double nValue, final int nScale)
	{
		final BigDecimal oOriginalValue = new BigDecimal(nValue);
		return oOriginalValue.setScale(nScale, RoundingMode.CEILING);
	}
}

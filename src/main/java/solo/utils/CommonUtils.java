package solo.utils;

import org.apache.commons.lang.StringUtils;

public class CommonUtils
{
	static public String splitFirst(final String strText)
	{
		return splitToPos(strText, 0);
	}

	static public String splitToPos(final String strText, final int nPos)
	{
		final String[] strParts = strText.split(" |_");
		return (nPos < strParts.length ? strParts[nPos] : StringUtils.EMPTY);
	}
	
	static public String splitTail(final String strText)
	{
		return splitTail(strText, 2);
	}
	
	static public String splitTail(final String strText, final int nPos)
	{
		final String[] aParts = strText.split(" |_", nPos);
		return (aParts.length > (nPos - 1) ? aParts[nPos - 1] : StringUtils.EMPTY);
	}
}

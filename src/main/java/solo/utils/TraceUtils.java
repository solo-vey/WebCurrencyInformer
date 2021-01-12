package solo.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class TraceUtils
{
	TraceUtils() 
	{
		throw new IllegalStateException("Utility class");
	}
	  
	public static void writeTrace(final String strText)
	{
		System.out.println(strText);
	}
	  
	public static void writeError(final String strText)
	{
		System.err.println(strText);
	}
	
	public static void writeError(final String strText, final Exception e)
	{
		final DateFormat oDateFormat = new SimpleDateFormat("HH:mm:ss");
		final String strDate = oDateFormat.format(new Date()); 
		
		TraceUtils.writeError(strDate + " " +Thread.currentThread().getName() + 
				(StringUtils.isNotBlank(strText) ? "\t" + strText : StringUtils.EMPTY) + 
				"\tThread exception : " + CommonUtils.getExceptionMessage(e));
	}
}

package solo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.HasParameters;

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
	
	static public Map<String, String> splitParameters(final String strText, final String strTemplate)
	{
		final Map<String, String> oParameters = new HashMap<String, String>();
		final String[] aTemplateParts = strTemplate.split(" |_");
		final String strLastTemplateName = aTemplateParts[aTemplateParts.length - 1]; 
		final String[] aParts = (HasParameters.TAIL_PARAMETER.equalsIgnoreCase(strLastTemplateName) ? strText.split(" |_", aTemplateParts.length) : strText.split(" |_"));
		
		for(int nPos = 0; nPos < aTemplateParts.length && nPos < aParts.length; nPos++)
		{
			if (StringUtils.isNotBlank(aParts[nPos].trim()))
				oParameters.put(aTemplateParts[nPos].replace("#", StringUtils.EMPTY).toLowerCase().trim(), aParts[nPos].trim());
		}
		
		return oParameters; 
	}
	
	static public String mergeParameters(final Object ... aParameters)
	{
		return StringUtils.join(aParameters, "_");
	}
	
	public static String encodeSha256HMAC(final String strSecretKey, final String strData) throws Exception 
	{
		  final Mac oSha256_HMAC = Mac.getInstance("HmacSHA256");
		  SecretKeySpec secret_key = new SecretKeySpec(strSecretKey.getBytes("UTF-8"), "HmacSHA256");
		  oSha256_HMAC.init(secret_key);

		  return Hex.encodeHexString(oSha256_HMAC.doFinal(strData.getBytes("UTF-8")));
	}
	
	public static String encodeSha256(final String strData) throws Exception 
	{
		final MessageDigest oDigest = MessageDigest.getInstance("SHA-256");
		final byte[] aHash = oDigest.digest(strData.getBytes(StandardCharsets.UTF_8));
		return Hex.encodeHexString(aHash);
	}

	public static String getExceptionMessage(final Throwable e)
	{
		if (null == e)
			return "Unknown. Cause == null";
		
		if (null != e.getMessage())
			return e.getMessage();
		
		if (null != e.getCause())
			return getExceptionMessage(e.getCause());
		
		return e.getMessage();
	}
}

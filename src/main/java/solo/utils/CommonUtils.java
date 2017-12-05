package solo.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
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
	
	public static String encodeSha256HMAC(final String strSecretKey, final String strData) throws Exception 
	{
		  final Mac oSha256_HMAC = Mac.getInstance("HmacSHA256");
		  SecretKeySpec secret_key = new SecretKeySpec(strSecretKey.getBytes("UTF-8"), "HmacSHA256");
		  oSha256_HMAC.init(secret_key);

		  return Hex.encodeHexString(oSha256_HMAC.doFinal(strData.getBytes("UTF-8")));
	}
}

package solo.utils;

import org.junit.Assert;
import org.junit.Test;

public class CommonUtilsTest
{
 	@Test
    public void testEncodeSha256HMAC() throws Exception 
    {
    	//	Arrange
    	
    	//	Act
    	final String strResult = CommonUtils.encodeSha256HMAC("AYifzxC3Xo", "GET|/api/v2/trades/my|access_key=dV6vEJe1CO&market=btcuah&tonce=1465850766246");

        //	Assert
    	Assert.assertEquals("33a694498a2a70cb4ca9a7e28224321e20b41f10217604e9de80ff4ee8cf310e", strResult);
    }
}
package solo.utils;

import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;

public class MathUtilsTest
{
 	@Test
    public void testToCurrencyStringEx2Zero() throws Exception 
    {
    	//	Arrange
    	
    	//	Act
    	final String strResult = MathUtils.toCurrencyStringEx2(new BigDecimal(0));
    	
        //	Assert
    	Assert.assertEquals("0", strResult);
    }
 	
 	@Test
    public void testToCurrencyStringEx2More10() throws Exception 
    {
    	//	Arrange
 		final BigDecimal nValue = new BigDecimal(123456789.34507);
    	
    	//	Act
    	final String strResult = MathUtils.toCurrencyStringEx2(nValue);
 	
        //	Assert
    	Assert.assertEquals("123 456 789.34507", strResult);
    }
 	
 	@Test
    public void testToCurrencyStringEx2Less0() throws Exception 
    {
    	//	Arrange
 		final BigDecimal nValue = new BigDecimal(-123456789.345007);
    	
    	//	Act
    	final String strResult = MathUtils.toCurrencyStringEx2(nValue);
 	
        //	Assert
    	Assert.assertEquals("-123 456 789.345007", strResult);
    }
 	
 	@Test
    public void testToCurrencyStringEx2Less10More0() throws Exception 
    {
    	//	Arrange
 		final BigDecimal nValue = new BigDecimal(3.345007);
    	
    	//	Act
    	final String strResult = MathUtils.toCurrencyStringEx2(nValue);
 	
        //	Assert
    	Assert.assertEquals("3.345007", strResult);
    }
}
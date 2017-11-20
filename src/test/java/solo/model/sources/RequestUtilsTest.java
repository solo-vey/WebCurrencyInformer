package solo.model.sources;

import junit.framework.Assert;

import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyRate;

public class RequestUtilsTest
{
    @Test
    public void testBestChangeBTC2UAH() throws Exception 
    {
    	//	Arrange
    	final ISource oBestChange = new BestChange(Currency.BTC, Currency.UAH);
    	
    	//	Act
    	final CurrencyRate oRate = oBestChange.getBuyRate();

        //	Assert
    	Assert.assertNotNull(oRate);
    	System.err.printf("Buy " + oRate + "\r\n");
    }

    @Test
    public void testBestChangeUAH2BTC() throws Exception 
    {
    	//	Arrange
    	final ISource oBestChange = new BestChange(Currency.BTC, Currency.UAH);
    	
    	//	Act
    	final CurrencyRate oRate = oBestChange.getSellRate();

        //	Assert
    	Assert.assertNotNull(oRate);
    	System.err.printf("Sell " + oRate + "\r\n");
    }
    
    @Test
    public void testBestChangeETH2UAH() throws Exception 
    {
    	//	Arrange
    	final ISource oBestChange = new BestChange(Currency.ETH, Currency.UAH);
    	
    	//	Act
    	final CurrencyRate oRate = oBestChange.getBuyRate();

        //	Assert
    	Assert.assertNotNull(oRate);
    	System.err.printf("Buy " + oRate + "\r\n");
    }

    @Test
    public void testBestChangeUAH2ETH() throws Exception 
    {
    	//	Arrange
    	final ISource oBestChange = new BestChange(Currency.ETH, Currency.UAH);
    	
    	//	Act
    	final CurrencyRate oRate = oBestChange.getSellRate();

        //	Assert
    	Assert.assertNotNull(oRate);
    	System.err.printf("Sell " + oRate + "\r\n");
    }
}
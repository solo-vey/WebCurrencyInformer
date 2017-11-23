package solo.model.stocks;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

public class KunaStockSourceTest
{
    @Test
    public void testKunaStockSource() throws Exception 
    {
    	//	Arrange
    	final IStockSource oKunaStockSource = StockExchangeFactory.getStockExchange(KunaStockExchange.NAME).getStockSource();
    	
    	//	Act
    	int nCount = 0;
    	while(true)
    	{
    		try
    		{
		    	final StockRateStates oStockRateStates = oKunaStockSource.getStockRates();
		    	StocksHistory.addHistory(oKunaStockSource.getStockExchange(), oStockRateStates);
		    	Thread.sleep(4000);
		    	System.err.printf("Count [" + nCount + "]. Date " + (new Date()) + "\r\n");
    		}
    		catch(Exception e) 
    		{
		    	System.err.printf("Count [" + nCount + "]. Exception. Date " + (new Date()) + "\r\n");
    		}
	    	nCount++;
    	}

        //	Assert
//    	Assert.assertNotNull(oStockRateStates);
 //   	System.err.printf(oStockRateStates.toString());
    }
}
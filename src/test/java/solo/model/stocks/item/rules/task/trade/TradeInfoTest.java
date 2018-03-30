package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import solo.BaseTest;
import solo.model.stocks.item.RateInfo;

public class TradeInfoTest extends BaseTest
{
    @Test public void testMinCriticalPrice() throws Exception
    {
    	//	Arrange
    	final TradeInfo oTradeInfo = new TradeInfo(RateInfo.ETH_UAH, 0);
    	oTradeInfo.addBuy(null, new BigDecimal(1000), new BigDecimal(0.1));
    	oTradeInfo.setCriticalPrice(new BigDecimal(10020));
    	
    	//	Act
    	final BigDecimal nMinCriticalPrice = oTradeInfo.getMinCriticalPrice();
    	
    	//	Assert
    	Assert.assertTrue(nMinCriticalPrice.compareTo(new BigDecimal(10000)) == 0);
    	
    }
	
}

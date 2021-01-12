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
    	oTradeInfo.addBuy(null, BigDecimal.valueOf(1000), BigDecimal.valueOf(0.1));
    	oTradeInfo.setCriticalPrice(BigDecimal.valueOf(10020));
    	
    	//	Act
    	final BigDecimal nMinCriticalPrice = oTradeInfo.getMinCriticalPrice();
    	
    	//	Assert
    	Assert.assertTrue(nMinCriticalPrice.compareTo(BigDecimal.valueOf(10000)) == 0);
    	
    }
	
}

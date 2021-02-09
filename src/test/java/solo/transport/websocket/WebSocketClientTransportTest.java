package solo.transport.websocket;

import java.io.IOException;

import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.stocks.item.RateInfo;
import solo.utils.ResourceUtils;

public class WebSocketClientTransportTest
{
    @Test
    public void publicApiUsageExampleTest() throws IOException 
    {
    	//	Arrange
		final String strPublicKey = ResourceUtils.getResource("trade.public.key", "ExmoStockExchange.properties");
		final String strSecretKey = ResourceUtils.getResource("trade.secret.key", "ExmoStockExchange.properties");
		final RateInfo oRateInfo = new RateInfo(Currency.BTC, Currency.USD);
    	
    	//	Act
		WebSocketClientTransport.createPublicWs(oRateInfo);
    	//WebSocketClientTransport.createProtectedWs(strPublicKey, strSecretKey);
    	
    	// wait for pressing any key
        System.in.read();

        //	Assert
    }
}
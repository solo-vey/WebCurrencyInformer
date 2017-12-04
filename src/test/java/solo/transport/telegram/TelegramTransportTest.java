package solo.transport.telegram;

import java.util.Map;

import org.junit.Test;

import solo.model.stocks.exchange.Stocks;
import solo.transport.ITransport;
import solo.transport.ITransportMessages;
import solo.transport.TransportFactory;

public class TelegramTransportTest
{
    @SuppressWarnings("unchecked")
	@Test
    public void testSendMessage() throws Exception 
    {
    	//	Arrange
    	final ITransport oTelegram = TransportFactory.getTransport(Stocks.Kuna);
    	
    	//	Act
    	final Map<String, Object> oResult = (Map<String, Object>) oTelegram.sendMessage("Привет");

        //	Assert
    	System.err.printf("Send message result " + oResult + "\r\n");
    }

	@Test
    public void testGetMessages() throws Exception 
    {
    	//	Arrange
    	final ITransport oTelegram = TransportFactory.getTransport(Stocks.Mock);
    	
    	//	Act
    	final ITransportMessages oMessages = oTelegram.getMessages();

        //	Assert
    	if (null != oMessages)
    		System.err.printf("Get messages result " + oMessages.getMessages().get(0).getText() + "\r\n");
    }
}
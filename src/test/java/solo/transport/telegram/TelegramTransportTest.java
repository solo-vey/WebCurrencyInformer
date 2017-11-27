package solo.transport.telegram;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import solo.transport.ITransport;
import solo.transport.ITransportMessage;
import solo.transport.ITransportMessages;
import solo.transport.TransportFactory;

public class TelegramTransportTest
{
    @SuppressWarnings("unchecked")
	@Test
    public void testSendMessage() throws Exception 
    {
    	//	Arrange
    	final ITransport oTelegram = TransportFactory.getTransport(TelegramTransport.NAME);
    	
    	//	Act
    	final Map<String, Object> oResult = (Map<String, Object>) oTelegram.sendMessage("Привет");

        //	Assert
    	System.err.printf("Send message result " + oResult + "\r\n");
    }

	@Test
    public void testGetMessages() throws Exception 
    {
    	//	Arrange
    	final ITransport oTelegram = TransportFactory.getTransport(TelegramTransport.NAME);
    	
    	//	Act
    	final ITransportMessages oMessages = oTelegram.getMessages();

        //	Assert
    	if (null != oMessages)
    		System.err.printf("Get messages result " + oMessages.getMessages().get(0).getText() + "\r\n");
    }
}
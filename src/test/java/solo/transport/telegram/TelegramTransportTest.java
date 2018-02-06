package solo.transport.telegram;

import java.util.Map;

import org.junit.Test;

import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;
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
	

	@SuppressWarnings("unchecked")
	@Test
    public void testDeleteMessage() throws Exception 
    {
		//		Arrange
		WorkerFactory.registerMainWorkerThread(Thread.currentThread().getId(), new MainWorker(Stocks.Exmo));
    	final ITransport oTelegram = TransportFactory.getTransport(Stocks.Exmo);
    	final Map<String, Object> oResult = (Map<String, Object>) oTelegram.sendMessage("Привет");
		final TelegramMessage oMessage = new TelegramMessage((Map<String, Object>)oResult.get("result"));

    	//	Act
		oTelegram.deleteMessage(oMessage.getID());
   	
        //	Assert
    	System.err.printf("Delete sended messages \r\n");
    }
}
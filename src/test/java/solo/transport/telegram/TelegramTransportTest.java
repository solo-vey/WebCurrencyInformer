package solo.transport.telegram;

import java.util.Map;

import org.junit.Test;

import solo.archive.transports.TransportFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.ITransport;
import solo.transport.ITransportMessages;
import solo.utils.TraceUtils;

public class TelegramTransportTest
{
    @SuppressWarnings("unchecked")
	@Test
    public void testSendMessage() 
    {
    	//	Arrange
    	final ITransport oTelegram = TransportFactory.getTransport(Stocks.Kuna);
    	
    	//	Act
    	final Map<String, Object> oResult = (Map<String, Object>) oTelegram.sendMessage("Привет");

        //	Assert
    	TraceUtils.writeTrace("Send message result " + oResult + "\r\n");
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
    		TraceUtils.writeTrace("Get messages result " + oMessages.getMessages().get(0).getText() + "\r\n");
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
    	TraceUtils.writeTrace("Delete sended messages \r\n");
    }
}
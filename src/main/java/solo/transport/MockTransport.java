package solo.transport;

import java.io.File;
import solo.transport.ITransport;
import solo.transport.ITransportMessages;

public class MockTransport implements ITransport
{
	@Override public String getName()
	{
		return "Mock";
	}

	@Override public Object sendMessage(final String strText) throws Exception
	{
		System.out.println(strText);
		return strText;
	}
    
	@Override public void sendPhoto(final File oPhoto, String strCaption) throws Exception
    { 
	} 

	@Override
	public ITransportMessages getMessages() throws Exception
	{
		return null;
	}

	@Override public String getProperties()
	{
		return  getName() + "Transport.properties";
	}

	@Override
	public void deleteMessage(String strMessageID) throws Exception
	{
	}
}

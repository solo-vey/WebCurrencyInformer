package solo.transport;

import java.io.File;

public interface ITransport
{
	Object sendMessage(final String strText);
	void sendPhoto(final File oPhoto, String strCaption) throws Exception;
	void deleteMessage(String strMessageID) throws Exception;
	ITransportMessages getMessages() throws Exception;
	String getName();
	String getProperties();
}

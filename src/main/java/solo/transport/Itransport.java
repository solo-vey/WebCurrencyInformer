package solo.transport;

import java.io.File;

public interface ITransport
{
	Object sendMessage(final String strText) throws Exception;
	void sendPhoto(final File oPhoto, String strCaption) throws Exception;
	ITransportMessages getMessages() throws Exception;
	String getName();
	String getProperties();
}

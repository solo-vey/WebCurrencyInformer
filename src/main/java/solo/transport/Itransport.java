package solo.transport;

public interface ITransport
{
	Object sendMessage(final String strText) throws Exception;
	ITransportMessages getMessages() throws Exception;
	String getName();
}

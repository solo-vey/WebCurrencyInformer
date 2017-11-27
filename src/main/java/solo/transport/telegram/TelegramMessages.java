package solo.transport.telegram;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import solo.transport.ITransportMessage;
import solo.transport.ITransportMessages;

public class TelegramMessages implements ITransportMessages
{
	private List<ITransportMessage> m_oMessages = new LinkedList<ITransportMessage>();

	@SuppressWarnings("unchecked")
	public TelegramMessages(final Map<String, Object> oData)
	{
    	final List<Object> oList = (List<Object>)oData.get("result");
    	for(final Object oMessage : oList)
    		m_oMessages.add(new TelegramMessage((Map<String, Object>)oMessage));
	}

	@Override public List<ITransportMessage> getMessages()
	{
		return m_oMessages ;
	}

}

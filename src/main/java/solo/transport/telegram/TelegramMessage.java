package solo.transport.telegram;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import solo.transport.ITransportMessage;

public class TelegramMessage implements ITransportMessage
{
	final private String m_strText;
	final private String m_strID;
	final Map<String, Object> m_oMessage;

	@SuppressWarnings("unchecked")
	public TelegramMessage(final Map<String, Object> oData)
	{
    	m_oMessage = (Map<String, Object> )oData.get("message");
   		m_strText = (null != m_oMessage && null != m_oMessage.get("text") ? m_oMessage.get("text").toString() : StringUtils.EMPTY);
		m_strID = oData.get("update_id").toString();
	}
	
	@Override public String getText()
	{
		return m_strText;
	}

	@Override public String getID()
	{
		return m_strID;
	}
}

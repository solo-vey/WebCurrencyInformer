package solo.transport.telegram;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import solo.transport.ITransportMessage;

public class TelegramMessage implements ITransportMessage
{
	final private String m_strText;
	final private String m_strID;
	final private String m_strUpdateID;
	final Map<String, Object> m_oMessage;

	@SuppressWarnings("unchecked")
	public TelegramMessage(final Map<String, Object> oData)
	{
    	m_oMessage =  (Map<String, Object> )(null != oData.get("message") ?oData.get("message") : oData.get("channel_post"));
   		m_strText = (null != m_oMessage && null != m_oMessage.get("text") ? m_oMessage.get("text").toString() : StringUtils.EMPTY);
		m_strID = (null != m_oMessage && null != m_oMessage.get("message_id") ? m_oMessage.get("message_id").toString() : StringUtils.EMPTY);
		m_strUpdateID = (null != oData.get("update_id") ? oData.get("update_id").toString() : StringUtils.EMPTY);
	}
	
	@Override public String getText()
	{
		return m_strText;
	}

	@Override public String getID()
	{
		return m_strID;
	}

	@Override public String getUpdateID()
	{
		return m_strUpdateID;
	}
}

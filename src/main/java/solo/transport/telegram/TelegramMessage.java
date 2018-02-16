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
		Map<String, Object> oMessage = (Map<String, Object> )(null != oData.get("message") ?oData.get("message") : oData.get("channel_post"));
		m_strUpdateID = (null != oData.get("update_id") ? oData.get("update_id").toString() : StringUtils.EMPTY);
   		String strText = (null != oMessage && null != oMessage.get("text") ? oMessage.get("text").toString() : StringUtils.EMPTY);
		
		if (null != oData.get("callback_query"))
		{
			final Map<String, Object> oCallbackQuery = (Map<String, Object>)oData.get("callback_query");
			oMessage = (Map<String, Object> )(null != oCallbackQuery.get("message") ? oCallbackQuery.get("message") : oMessage);
			strText = (null != oCallbackQuery && null != oCallbackQuery.get("data") ? oCallbackQuery.get("data").toString() : strText);
		}
		
		m_oMessage = oMessage;
   		m_strText = strText;
		m_strID = (null != oMessage && null != oMessage.get("message_id") ? oMessage.get("message_id").toString() : StringUtils.EMPTY);
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

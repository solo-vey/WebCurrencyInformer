package solo.transport.telegram;

import java.util.HashMap;
import java.util.Map;

import solo.transport.ITransport;
import solo.transport.ITransportMessages;
import solo.utils.RequestUtils;
import ua.lz.ep.utils.ResourceUtils;

public class TelegramTransport implements ITransport
{
	public static final String NAME = "Telegram";
	
	protected static final String PROPERTIES = "TelegramTransport.properties";
	protected final static String API_URL = "https://api.telegram.org/bot492426738:AAFCCVUkQX20WwVx6m8efsd0tbpE-WEH99U/METHOD_NAME";
	protected final static String SEND_MESSAGE_URL = API_URL.replace("METHOD_NAME", "sendMessage");
	protected final static String GET_UPDATES_URL = API_URL.replace("METHOD_NAME", "getUpdates");
	protected final static String CHAT_ID = "492426738";
	protected final static String USER_ID = "395270842";
	
	protected final static Integer GET_UPDATES_TIMEOUT = ResourceUtils.getIntFromResource("getUpdates.timeout", PROPERTIES, 1);
	
	protected Integer m_nLastReceiveMessageID = 940309572;
	
	@Override public String getName()
	{
		return NAME;
	}
	
	@Override public Object sendMessage(final String strText) throws Exception
	{
		final Map<String, String> aParameters = new HashMap<String, String>();
		aParameters.put("chat_id", USER_ID);
		aParameters.put("text", strText);
		return RequestUtils.sendPostAndReturnJson(SEND_MESSAGE_URL, aParameters, true);
	}

	@Override
	public ITransportMessages getMessages() throws Exception
	{
		final Map<String, String> aParameters = new HashMap<String, String>();
		aParameters.put("timeout", GET_UPDATES_TIMEOUT.toString());
		aParameters.put("offset", "-1");
		final Map<String, Object> oResult = RequestUtils.sendPostAndReturnJson(GET_UPDATES_URL, aParameters, true);
		final TelegramMessages oMessages = new TelegramMessages(oResult);
		
		if (oMessages.getMessages().size() == 0)
			return null;
		
		final String strLastMessageID = oMessages.getMessages().get(oMessages.getMessages().size() - 1).getID();
		final Integer nNextMessageID = Integer.parseInt(strLastMessageID) + 1;
		aParameters.put("offset", nNextMessageID.toString());
		RequestUtils.sendPostAndReturnJson(GET_UPDATES_URL, aParameters, true);
		return oMessages;
	}
}

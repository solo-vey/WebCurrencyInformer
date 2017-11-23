package solo.transport.telegram;

import java.util.HashMap;
import java.util.Map;

import solo.transport.Itransport;
import solo.utils.RequestUtils;

public class TelegramTransport implements Itransport
{
	protected final static String API_URL = "https://api.telegram.org/bot492426738:AAFCCVUkQX20WwVx6m8efsd0tbpE-WEH99U/METHOD_NAME";
	protected final static String SEND_MESSAGE_URL = API_URL.replace("METHOD_NAME", "sendMessage");
	protected final static String CHAT_ID = "492426738";
	protected final static String USER_ID = "395270842";
	
	@Override public void sendMessage(String strText) throws Exception
	{
		final Map<String, String> aParameters = new HashMap<String, String>();
		aParameters.put("chat_id", USER_ID);
		aParameters.put("text", "Hello");
		RequestUtils.sendPost(SEND_MESSAGE_URL, aParameters, true);
	}
}

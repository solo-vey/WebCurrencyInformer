package solo.transport.telegram;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import solo.CurrencyInformer;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.ITransport;
import solo.transport.ITransportMessages;
import solo.utils.JsonUtils;
import solo.utils.RequestUtils;
import solo.utils.ResourceUtils;

public class TelegramTransport implements ITransport
{
	private static final String SYSTEM_MESSGE = "SYSTEM";

	private static final String MANAGER_MESSGE = "MANAGER";

	protected static final String API_URL = "https://api.telegram.org/bot#ACCESS_TOKEN#/METHOD_NAME";
	
	protected final String m_strBotName;
	protected final String m_strBotAccessToken;
	protected final String m_strProperies;
	protected final String m_strUserID;
	protected final String m_strSystemUserID;
	protected final Integer m_nGetUpdatesTimeout;
	protected Integer m_nNextMessageID = null; 
	protected String m_strSystemMessageID = null; 
	
	public TelegramTransport()
	{
		this(StringUtils.EMPTY);
	}
	
	public TelegramTransport(final String strBotName)
	{
		m_strBotName = strBotName;
		m_strProperies = strBotName + "TelegramTransport.properties";
		m_strBotAccessToken = ResourceUtils.getResource("accessToken", getProperties());
		m_strUserID = ResourceUtils.getResource("user_id", getProperties());
		m_strSystemUserID = ResourceUtils.getResource("system_user_id", getProperties());
		m_nGetUpdatesTimeout = ResourceUtils.getIntFromResource("getUpdates.timeout", getProperties(), 4);
		m_nNextMessageID = ResourceUtils.getIntFromResource("start_message_id", getProperties(), -1);
	}
	
	@Override public String getName()
	{
		return m_strBotName + "Telegram";
	}
	
	protected String getApiUrl()
	{
		return API_URL.replace("#ACCESS_TOKEN#", m_strBotAccessToken);
	}
	
	protected String getSendMessageUrl()
	{
		return getApiUrl().replace("METHOD_NAME", "sendMessage");
	}
	
	protected String getSendPhotoUrl()
	{
		return getApiUrl().replace("METHOD_NAME", "sendPhoto");
	}
	
	protected String getUpdatesUrl()
	{
		return getApiUrl().replace("METHOD_NAME", "getUpdates");
	}
	
	protected String getDeleteMessageUrl()
	{
		return getApiUrl().replace("METHOD_NAME", "deleteMessage");
	}
	
	@Override public Object sendMessage(final String strText)
	{
		if (StringUtils.isBlank(strText))
			return null;
		
		try
		{
			final String strType = getMessageType(strText);
			final boolean bIsSystem = SYSTEM_MESSGE.equalsIgnoreCase(strType);
			deleteLastSystemMessage(bIsSystem);
			
			final Map<String, String> aParameters = new HashMap<>();
			aParameters.put("chat_id", (StringUtils.isNotBlank(strType) ? m_strSystemUserID : m_strUserID));
			aParameters.put("text", getMessageText(strText));
			
			final String strButtons = getMessageButtons(strText);
			if (StringUtils.isNotBlank(strButtons))
				aParameters.put("reply_markup", strButtons);
			
			final String strParseMode = getParseMode(strText);
			if (StringUtils.isNotBlank(strParseMode))
				aParameters.put("parse_mode", strParseMode);
			
			final Map<String, Object> oResult = RequestUtils.sendPostAndReturnJson(getSendMessageUrl(), aParameters, true, RequestUtils.DEFAULT_TEMEOUT);
			oResult.put("message", oResult.get("result"));
			final TelegramMessage oMessage = new TelegramMessage(oResult);
			saveLastSystemMessageID(bIsSystem, oMessage.getID());
			return oResult;
		}
		catch(final Exception e) 
		{
			WorkerFactory.onException("Can't send message", e);
		}
		
		return null;
	}
	
	public String getParseMode(final String strText)
	{
		if (strText.contains("MARKDOWN_STYLE\r\n") || strText.contains("`"))
			return "Markdown";
		if (strText.contains("HTML_STYLE\r\n") || strText.contains("<code>") || strText.contains("<b>") || strText.contains("<i>"))
			return "HTML";
		
		return "HTML";
	}
    
	@Override public void sendPhoto(final File oPhoto, String strCaption) throws Exception
    { 
		deleteLastSystemMessage(true);
    	 
		final MultipartEntityBuilder oBuilder = MultipartEntityBuilder.create(); 
		oBuilder.addTextBody("chat_id", m_strSystemUserID, ContentType.TEXT_PLAIN); 
		oBuilder.addBinaryBody("photo", oPhoto, ContentType.APPLICATION_OCTET_STREAM, oPhoto.getName()); 
    	if (null != strCaption) 
    		oBuilder.addTextBody("caption", getMessageText(strCaption), ContentType.TEXT_PLAIN);
    	
		final String strButtons = getMessageButtons("SYSTEM\r\n" + strCaption);
		if (StringUtils.isNotBlank(strButtons))
			oBuilder.addTextBody("reply_markup", strButtons);
		
		final String strParseMode = getParseMode(strCaption);
		if (StringUtils.isNotBlank(strParseMode))
			oBuilder.addTextBody("parse_mode", strParseMode);
		
    	final String strResult = uploadFileRequest(getSendPhotoUrl(), oBuilder, false);
    	final Map<String, Object> oResult = JsonUtils.json2Map(strResult);
    	oResult.put("message", oResult.get("result"));
    	final TelegramMessage oMessage = new TelegramMessage(oResult);
    	
    	saveLastSystemMessageID(true, oMessage.getID());
  	}	
	
	protected String getMessageType(final String strMessage)
	{
		if (strMessage.startsWith("SYSTEM\r\n"))
			return SYSTEM_MESSGE;
		
		if (strMessage.startsWith("MANAGER\r\n"))
			return MANAGER_MESSGE;
		
		return StringUtils.EMPTY;
	}
	
	protected String getMessageText(final String strMessage)
	{
		String strText = strMessage.replace("SYSTEM\r\n", StringUtils.EMPTY)
						.replace("MARKDOWN_STYLE\r\n", StringUtils.EMPTY)
						.replace("HTML_STYLE\r\n", StringUtils.EMPTY)
						.replace("BUTTONS\r\n", "\0").split("\0")[0];
		
		final String strParseMode = getParseMode(strMessage);
		final String strStockPrefix = (strParseMode.equalsIgnoreCase("Markdown") ? 
										"*@" + WorkerFactory.getMainWorker().getStock() + "*\r\n" : 
										"<b>@" + WorkerFactory.getMainWorker().getStock() + "</b>\r\n");
		
		strText = strStockPrefix + strText;
		if (strText.length() < 4096)
			return strText;
		
		strText = strText.substring(strText.length() - 4000);
		final String[] aTextParts = strText.split("\r", 2);
		return strStockPrefix + "..." + (aTextParts.length > 1 ? aTextParts[1] : aTextParts[0]);
	}
	
	protected String getMessageButtons(final String strMessage)
	{
		if (!SYSTEM_MESSGE.equalsIgnoreCase(getMessageType(strMessage)))
			return StringUtils.EMPTY;
		
		final boolean bHasButtons = (strMessage.replace("BUTTONS\r\n", "\0").split("\0").length > 1);
		final String strButtons = (bHasButtons ? strMessage.replace("BUTTONS\r\n", "\0").split("\0")[1] : StringUtils.EMPTY);
		final String strSystemButtons = getButtons(Arrays.asList(Arrays.asList("Money=info", "Rules=rules", "Manager=manager_day", "Rates=rate")));
		return "{\"inline_keyboard\":[" + strSystemButtons + 
						(StringUtils.isNotBlank(strButtons) ? "," + strButtons : StringUtils.EMPTY) + "]}";
	}
	
	public static String getButtons(final List<List<String>> aButtons)
	{
		String strButtons = StringUtils.EMPTY;
		boolean bIsFirstLine = true;
		for(final List<String> oLine : aButtons)
		{
			strButtons += (bIsFirstLine ? StringUtils.EMPTY : ",") + "[";
			boolean bIsFirstButton = true;
			for(final String strButtonInfo : oLine)
			{
				final String[] aButtonParts = strButtonInfo.split("=");
				if (aButtonParts.length < 2)
					continue;
				
				final String strButtonText = aButtonParts[0].replace("\"", "'").replace("{", "'").replace("}", "'").replace("\r\n", "");
				final String strButtonCommand = aButtonParts[aButtonParts.length - 1];
				strButtons += (bIsFirstButton ? StringUtils.EMPTY : ",") + "{\"text\":\"" + strButtonText + "\",\"callback_data\":\"" + strButtonCommand + "\"}";
				bIsFirstButton = false;
			}
			strButtons += "]";
			bIsFirstLine = false;
		}
		return strButtons;
	}
	
	@Override public void deleteMessage(final String strMessageID) throws Exception
	{
		if (StringUtils.isBlank(strMessageID))
			return;
		
		try
		{
			final Map<String, String> aParameters = new HashMap<>();
			aParameters.put("chat_id", m_strSystemUserID);
			aParameters.put("message_id", strMessageID);
			RequestUtils.sendPostAndReturnJson(getDeleteMessageUrl(), aParameters, true, RequestUtils.DEFAULT_TEMEOUT);
		}
		catch(final Exception e) {/***/}
	}	

	void deleteLastSystemMessage(final boolean bIsSystem) throws Exception
	{
		if (!bIsSystem || null == m_strSystemMessageID)
			return;

		deleteMessage(m_strSystemMessageID);
		m_strSystemMessageID = null;
	} 
	
	void saveLastSystemMessageID(final boolean bIsSystem, final String strMessageID)
	{
		if (bIsSystem)
			m_strSystemMessageID = strMessageID;
	}
    
    private String uploadFileRequest(String url, MultipartEntityBuilder builder, Boolean returnAllJson) throws Exception
    { 
    	CloseableHttpClient httpClient = HttpClients.createDefault(); 
    	HttpPost uploadFile = new HttpPost(url);

    	final String strProxyHost = ResourceUtils.getResource("proxy.host", CurrencyInformer.PROPERTIES_FILE_NAME);
		final int nProxyPort = ResourceUtils.getIntFromResource("proxy.port", CurrencyInformer.PROPERTIES_FILE_NAME, 0);
		
		if (!StringUtils.isBlank(strProxyHost) && 0 != nProxyPort)
		{
			RequestConfig requestConfig = RequestConfig.custom()
		    		.setProxy(new HttpHost(strProxyHost, nProxyPort))
		    		.build();
			uploadFile.setConfig(requestConfig);		
		}
    	  
    	HttpEntity multipart = builder.build(); 
    	uploadFile.setEntity(multipart); 
    	 
    	CloseableHttpResponse postResponse = httpClient.execute(uploadFile); 
    	HttpEntity responseEntity = postResponse.getEntity(); 
    	   
    	return IOUtils.toString(responseEntity.getContent());
    }

	@Override
	public ITransportMessages getMessages() throws Exception
	{
		final Map<String, String> aParameters = new HashMap<>();
		aParameters.put("timeout", m_nGetUpdatesTimeout.toString());
		aParameters.put("offset", m_nNextMessageID.toString());
		aParameters.put("limit", "1");
		final Map<String, Object> oResult = RequestUtils.sendPostAndReturnJson(getUpdatesUrl(), aParameters, true, m_nGetUpdatesTimeout + 1);
		final TelegramMessages oMessages = new TelegramMessages(oResult);
		
		if (oMessages.getMessages().isEmpty())
			return null;
		
		final String strLastMessageID = oMessages.getMessages().get(oMessages.getMessages().size() - 1).getUpdateID();
		m_nNextMessageID = Integer.parseInt(strLastMessageID) + 1;
		return oMessages;
	}

	public Integer getTimeOut()
	{
		return (null == m_nNextMessageID ? 0 : m_nGetUpdatesTimeout); 
	}
	
	public Integer getOffset()
	{
		return (null == m_nNextMessageID ? -1 : m_nNextMessageID); 
	}
	
	public String getProperties()
	{
		return m_strProperies;
	}
}

package solo.transport.telegram;

import java.io.File;
import java.util.HashMap;
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
import solo.transport.ITransport;
import solo.transport.ITransportMessages;
import solo.utils.RequestUtils;
import ua.lz.ep.utils.ResourceUtils;

public class TelegramTransport implements ITransport
{
	protected final static String API_URL = "https://api.telegram.org/bot#ACCESS_TOKEN#/METHOD_NAME";
	
	final protected String m_strBotName;
	final protected String m_strBotAccessToken;
	final protected String m_strProperies;
	final protected String m_strUserID;
	final protected Integer m_nGetUpdatesTimeout;
	protected Integer m_nNextMessageID = null; 
	
	public TelegramTransport(final String strBotName)
	{
		m_strBotName = strBotName;
		m_strProperies = strBotName + "TelegramTransport.properties";
		m_strBotAccessToken = ResourceUtils.getResource("accessToken", getProperties());
		m_strUserID = ResourceUtils.getResource("user_id", getProperties());
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
	
	@Override public Object sendMessage(final String strText) throws Exception
	{
		if (StringUtils.isBlank(strText))
			return null;
		
		final Map<String, String> aParameters = new HashMap<String, String>();
		aParameters.put("chat_id", m_strUserID);
		aParameters.put("text", strText);
		return RequestUtils.sendPostAndReturnJson(getSendMessageUrl(), aParameters, true, RequestUtils.DEFAULT_TEMEOUT);
	}
	
    
	@Override public void sendPhoto(final File oPhoto, String strCaption) throws Exception
    { 
    	  MultipartEntityBuilder builder = MultipartEntityBuilder.create(); 
    	  builder.addTextBody("chat_id", m_strUserID, ContentType.TEXT_PLAIN); 
    	  builder.addBinaryBody("photo", oPhoto, ContentType.APPLICATION_OCTET_STREAM, oPhoto.getName()); 
    	  if (null != strCaption) 
    		  builder.addTextBody("caption", strCaption, ContentType.TEXT_PLAIN);
    	  
    	  uploadFileRequest(getSendPhotoUrl(), builder, false);
  	 } 
    
    private String uploadFileRequest(String url, MultipartEntityBuilder builder, Boolean returnAllJson) throws Exception
    { 
    	CloseableHttpClient httpClient = HttpClients.createDefault(); 
    	HttpPost uploadFile = new HttpPost(url);

    	final String strProxyHost = ResourceUtils.getResource("proxy.host", CurrencyInformer.PROPERTIES_FILE_NAME);
		final int nProxyPort = ResourceUtils.getIntFromResource("proxy.port", CurrencyInformer.PROPERTIES_FILE_NAME, 0);
		
		RequestConfig requestConfig = RequestConfig.custom()
	    		.setProxy(new HttpHost(strProxyHost, nProxyPort))
	    		.build();
		uploadFile.setConfig(requestConfig);		
    	  
    	HttpEntity multipart = builder.build(); 
    	 
    	uploadFile.setEntity(multipart); 
    	 
    	CloseableHttpResponse postResponse = httpClient.execute(uploadFile); 
    	HttpEntity responseEntity = postResponse.getEntity(); 
    	   
    	return IOUtils.toString(responseEntity.getContent());
    }

	@Override
	public ITransportMessages getMessages() throws Exception
	{
		final Map<String, String> aParameters = new HashMap<String, String>();
		aParameters.put("timeout", m_nGetUpdatesTimeout.toString());
		aParameters.put("offset", m_nNextMessageID.toString());
		aParameters.put("limit", "1");
		final Map<String, Object> oResult = RequestUtils.sendPostAndReturnJson(getUpdatesUrl(), aParameters, true, m_nGetUpdatesTimeout + 1);
		final TelegramMessages oMessages = new TelegramMessages(oResult);
		
		if (oMessages.getMessages().size() == 0)
			return null;
		
		final String strLastMessageID = oMessages.getMessages().get(oMessages.getMessages().size() - 1).getID();
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

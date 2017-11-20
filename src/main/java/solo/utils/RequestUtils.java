package solo.utils;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solo.CurrencyInformer;
import ua.lz.ep.utils.ResourceUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/** Класс для работы с РЕЕЗ запросами к сторонним сервисам */
@SuppressWarnings("deprecation")
public class RequestUtils
{
    /** The Constant s_oLogger. */
    final static Logger s_oLogger = LoggerFactory.getLogger(RequestUtils.class);
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static String sendPost(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, aParameters);
		return sendPostRequestAndReturnText(oPost, bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param strJsonParameters Параметровы запроса 
	 * @param aHeaders Параметры в заголовке
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static String sendPost(final String strURL, String strJsonParameters, final Map<String, String> aHeaders, final Boolean bIsUseProxy) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, strJsonParameters, aHeaders);
		return sendPostRequestAndReturnText(oPost, bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, String> sendPostAndReturnJson(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, aParameters);
		return sendPostRequestAndReturnJson(oPost, bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param strJsonParameters Параметровы запроса 
	 * @param aHeaders Параметры в заголовке
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, String> sendPostAndReturnJson(final String strURL, String strJsonParameters, final Map<String, String> aHeaders, final Boolean bIsUseProxy) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, strJsonParameters, aHeaders);
		return sendPostRequestAndReturnJson(oPost, bIsUseProxy);
	}

	/** Формирование запроса на основании указанных параметров 
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @return HTTP запрос
	 * @throws UnsupportedEncodingException  */
	static HttpPost makePostQuery(final String strURL, final Map<String, String> aParameters) throws UnsupportedEncodingException
	{
		final HttpPost oPost = new HttpPost(strURL);
		final ArrayList<NameValuePair> aPostParameters = new ArrayList<NameValuePair>();
		for(final Entry<String, String> oParameter : aParameters.entrySet())
			aPostParameters.add(new BasicNameValuePair(oParameter.getKey(), oParameter.getValue()));
		oPost.setEntity(new UrlEncodedFormEntity(aPostParameters));
		return oPost;
	}

	/** Формирование запроса на основании указанных параметров 
	 * @param strURL URL запроса
	 * @param strJsonParameters Параметровы запроса 
	 * @param aHeaders Параметры в заголовке
	 * @return HTTP запрос
	 * @throws UnsupportedEncodingException */
	static HttpPost makePostQuery(final String strURL, String strJsonParameters, final Map<String, String> aHeaders)
			throws UnsupportedEncodingException
	{
		final HttpPost oPost = new HttpPost(strURL);
		oPost.setEntity(new StringEntity((strJsonParameters)));
		for(final Entry<String, String> oHeader : aHeaders.entrySet())
			oPost.addHeader(oHeader.getKey(), oHeader.getValue());
		return oPost;
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param oPost Запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, String> sendPostRequestAndReturnJson(final HttpPost oPost, final Boolean bIsUseProxy) throws Exception
	{
		try
		{	
			final String strJson = sendPostRequestAndReturnText(oPost, bIsUseProxy);
			final Gson oGson = new Gson();
			final Type oMapType = new TypeToken<Map<String, String>>(){}.getType();
			return oGson.fromJson(strJson, oMapType);
		}
		catch (final Exception e)
		{
            s_oLogger.error("Error executing post query [" + oPost + "]", e);
            throw e; 
		}
	}

	/** Отправдяеи post запрос по указанному адресу
	 * @param oPost Запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static String sendPostRequestAndReturnText(final HttpPost oPost, final Boolean bIsUseProxy) throws Exception
	{
		try
		{	
			final HttpClient oClient = new DefaultHttpClient();
			setProxy(oClient, bIsUseProxy);
			final HttpResponse oResponse = oClient.execute(oPost);
			if (null == oResponse)
			    return null;

			if (oResponse.getStatusLine().getStatusCode() != 200)
			    throw new Exception("Query response status != 200.\r\n Status line [" + oResponse.getStatusLine() + "]");
			
			final InputStream oSource = (InputStream) oResponse.getEntity().getContent();
			final StringWriter oWriter = new StringWriter();
			IOUtils.copy(oSource, oWriter, "UTF-8");
			return oWriter.toString();
		}
		catch (final Exception e)
		{
            s_oLogger.error("Error executing post query [" + oPost + "]", e);
            throw e; 
		}
	}
	
	/** Устанавливаем прокси если нужно 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса */
	public static void setProxy(final  HttpClient oClient, final Boolean bIsUseProxy)
	{
		if (!bIsUseProxy)
			return;
		
		final String strProxyHost = ResourceUtils.getResource("proxy.host", CurrencyInformer.PROPERTIES_FILE_NAME);
		final int nProxyPort = ResourceUtils.getIntFromResource("proxy.port", CurrencyInformer.PROPERTIES_FILE_NAME, 0);
		
		if (StringUtils.isBlank(strProxyHost) || 0 == nProxyPort)
			return;
		
		final HttpHost oProxy = new HttpHost(strProxyHost, nProxyPort);
		oClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, oProxy);
	}
}

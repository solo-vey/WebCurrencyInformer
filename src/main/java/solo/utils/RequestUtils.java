package solo.utils;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solo.CurrencyInformer;
import ua.lz.ep.utils.JsonUtils;
import ua.lz.ep.utils.ResourceUtils;

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
	public static String sendGet(final String strURL, final Boolean bIsUseProxy) throws Exception
	{
		return sendGet(strURL, new HashMap<String, String>(), bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static String sendGet(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy) throws Exception
	{
		final HttpGet oGet = makeGetQuery(strURL, aParameters);
		return sendRequestAndReturnText(oGet, bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static Map<String, Object> sendGetAndReturnMap(final String strURL, final Boolean bIsUseProxy) throws Exception
	{
		return sendGetAndReturnMap(strURL, new HashMap<String, String>(), bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, Object> sendGetAndReturnMap(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy) throws Exception
	{
		final HttpGet oGet = makeGetQuery(strURL, aParameters);
		return sendRequestAndReturnMap(oGet, bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static List<Object> sendGetAndReturnList(final String strURL, final Boolean bIsUseProxy) throws Exception
	{
		return sendGetAndReturnList(strURL, new HashMap<String, String>(), bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static List<Object> sendGetAndReturnList(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy) throws Exception
	{
		final HttpGet oGet = makeGetQuery(strURL, aParameters);
		return sendRequestAndReturnList(oGet, bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static <T extends Object> T sendGetAndReturnObject(final String strURL, final Boolean bIsUseProxy, final Class<T> oClass) throws Exception
	{
		return sendGetAndReturnObject(strURL, new HashMap<String, String>(), bIsUseProxy, oClass);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static <T extends Object> T sendGetAndReturnObject(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy, final Class<T> oClass) throws Exception
	{
		final HttpGet oGet = makeGetQuery(strURL, aParameters);
		return sendRequestAndReturnObject(oGet, bIsUseProxy, oClass);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static String sendPost(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, aParameters, null);
		return sendRequestAndReturnText(oPost, bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static String sendPost(final String strURL, final Map<String, String> aParameters, final Map<String, String> aHeaders, final Boolean bIsUseProxy) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, aParameters, aHeaders);
		return sendRequestAndReturnText(oPost, bIsUseProxy);
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
		return sendRequestAndReturnText(oPost, bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, Object> sendPostAndReturnJson(final String strURL, final Map<String, String> aParameters, final Map<String, String> aHeaders, final Boolean bIsUseProxy) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, aParameters, aHeaders);
		return sendRequestAndReturnMap(oPost, bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, Object> sendPostAndReturnJson(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, aParameters, null);
		return sendRequestAndReturnMap(oPost, bIsUseProxy);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param strJsonParameters Параметровы запроса 
	 * @param aHeaders Параметры в заголовке
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, Object> sendPostAndReturnJson(final String strURL, String strJsonParameters, final Map<String, String> aHeaders, final Boolean bIsUseProxy) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, strJsonParameters, aHeaders);
		return sendRequestAndReturnMap(oPost, bIsUseProxy);
	}

	/** Формирование запроса на основании указанных параметров 
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @return HTTP запрос
	 * @throws UnsupportedEncodingException  */
	static HttpGet makeGetQuery(final String strURL, final Map<String, String> aParameters) throws UnsupportedEncodingException
	{
		final HttpGet oGet = new HttpGet(strURL);
		return oGet;
	}

	/** Формирование запроса на основании указанных параметров 
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @return HTTP запрос
	 * @throws UnsupportedEncodingException  */
	static HttpPost makePostQuery(final String strURL, final Map<String, String> aParameters, final Map<String, String> aHeaders) throws UnsupportedEncodingException
	{
		final HttpPost oPost = new HttpPost(strURL);
		final ArrayList<NameValuePair> aPostParameters = new ArrayList<NameValuePair>();
		for(final Entry<String, String> oParameter : aParameters.entrySet())
			aPostParameters.add(new BasicNameValuePair(oParameter.getKey(), oParameter.getValue()));
		oPost.setEntity(new UrlEncodedFormEntity(aPostParameters));

		if (null != aHeaders)
		{
			for(final Entry<String, String> oHeader : aHeaders.entrySet())
				oPost.addHeader(oHeader.getKey(), oHeader.getValue());
		}
		
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
	protected static Map<String, Object> sendRequestAndReturnMap(final HttpUriRequest oHttpUriRequest, final Boolean bIsUseProxy) throws Exception
	{
		try
		{	
			final String strJson = sendRequestAndReturnText(oHttpUriRequest, bIsUseProxy);
			return JsonUtils.json2Map(strJson);
		}
		catch (final Exception e)
		{
            s_oLogger.error("Error executing query [" + oHttpUriRequest + "]", e);
            throw e; 
		}
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param oPost Запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	protected static List<Object> sendRequestAndReturnList(final HttpUriRequest oHttpUriRequest, final Boolean bIsUseProxy) throws Exception
	{
		try
		{	
			final String strJson = sendRequestAndReturnText(oHttpUriRequest, bIsUseProxy);
			return JsonUtils.json2List(strJson);
		}
		catch (final Exception e)
		{
            s_oLogger.error("Error executing query [" + oHttpUriRequest + "]", e);
            throw e; 
		}
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param oPost Запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	protected static <T extends Object> T sendRequestAndReturnObject(final HttpUriRequest oHttpUriRequest, final Boolean bIsUseProxy, final Class<T> oClass) throws Exception
	{
		try
		{	
			final String strJson = sendRequestAndReturnText(oHttpUriRequest, bIsUseProxy);
			return JsonUtils.fromJson(strJson, oClass);
		}
		catch (final Exception e)
		{
            s_oLogger.error("Error executing query [" + oHttpUriRequest + "]", e);
            throw e; 
		}
	}

	/** Отправдяеи post запрос по указанному адресу
	 * @param oPost Запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static String sendRequestAndReturnText(final HttpUriRequest oHttpUriRequest, final Boolean bIsUseProxy) throws Exception
	{
		try
		{	
			final HttpClient oClient = new DefaultHttpClient();
			setProxy(oClient, bIsUseProxy);
			final HttpResponse oResponse = oClient.execute(oHttpUriRequest);
			if (null == oResponse)
			    return null;

			if (oResponse.getStatusLine().getStatusCode() != 200 && oResponse.getStatusLine().getStatusCode() != 201 && oResponse.getStatusLine().getStatusCode() != 202)
			    throw new Exception("Query response status != 200.\r\n Status line [" + oResponse.getStatusLine() + "]");
			
			final InputStream oSource = (InputStream) oResponse.getEntity().getContent();
			final StringWriter oWriter = new StringWriter();
			IOUtils.copy(oSource, oWriter, "UTF-8");
			return oWriter.toString();
		}
		catch (final Exception e)
		{
            s_oLogger.error("Error executing query [" + oHttpUriRequest + "]", e);
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

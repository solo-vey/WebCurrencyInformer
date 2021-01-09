package solo.utils;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import solo.CurrencyInformer;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.worker.WorkerFactory;

/** Класс для работы с РЕЕЗ запросами к сторонним сервисам */
public class RequestUtils
{
	public final static int DEFAULT_TEMEOUT = 3;
	public final static int MAX_PARALEL_QUERY = 8;
	
	private static final Map<String, Semaphore> s_oAllSemaphores = new ConcurrentHashMap<String, Semaphore>();
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static String sendGet(final String strURL, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		return sendGet(strURL, new HashMap<String, String>(), bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static String sendGet(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		final HttpGet oGet = makeGetQuery(strURL, aParameters);
		return sendRequestAndReturnText(oGet, bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static Map<String, Object> sendGetAndReturnMap(final String strURL, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		return sendGetAndReturnMap(strURL, new HashMap<String, String>(), bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, Object> sendGetAndReturnMap(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		final HttpGet oGet = makeGetQuery(strURL, aParameters);
		return sendRequestAndReturnMap(oGet, bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static List<Object> sendGetAndReturnList(final String strURL, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		return sendGetAndReturnList(strURL, new HashMap<String, String>(), bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static List<Object> sendGetAndReturnList(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		final HttpGet oGet = makeGetQuery(strURL, aParameters);
		return sendRequestAndReturnList(oGet, bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static <T extends Object> T sendGetAndReturnObject(final String strURL, final Boolean bIsUseProxy, final Class<T> oClass, final int nTimeOut) throws Exception
	{
		return sendGetAndReturnObject(strURL, new HashMap<String, String>(), bIsUseProxy, oClass, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static <T extends Object> T sendGetAndReturnObject(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy, final Class<T> oClass, final int nTimeOut) throws Exception
	{
		final HttpGet oGet = makeGetQuery(strURL, aParameters);
		return sendRequestAndReturnObject(oGet, bIsUseProxy, oClass, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static String sendPost(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, aParameters, null);
		return sendRequestAndReturnText(oPost, bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static String sendPost(final String strURL, final Map<String, String> aParameters, final Map<String, String> aHeaders, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, aParameters, aHeaders);
		return sendRequestAndReturnText(oPost, bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param strJsonParameters Параметровы запроса 
	 * @param aHeaders Параметры в заголовке
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде строки 
	 * @throws Exception */
	public static String sendPost(final String strURL, String strJsonParameters, final Map<String, String> aHeaders, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, strJsonParameters, aHeaders);
		return sendRequestAndReturnText(oPost, bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, Object> sendPostAndReturnJson(final String strURL, final Map<String, String> aParameters, final Map<String, String> aHeaders, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, aParameters, aHeaders);
		return sendRequestAndReturnMap(oPost, bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param aParameters список параметров запроса 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, Object> sendPostAndReturnJson(final String strURL, final Map<String, String> aParameters, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, aParameters, null);
		return sendRequestAndReturnMap(oPost, bIsUseProxy, nTimeOut);
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param strURL URL запроса
	 * @param strJsonParameters Параметровы запроса 
	 * @param aHeaders Параметры в заголовке
	 * @param bIsUseProxy Использовать прокси при выполнении запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static Map<String, Object> sendPostAndReturnJson(final String strURL, String strJsonParameters, final Map<String, String> aHeaders, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		final HttpPost oPost = makePostQuery(strURL, strJsonParameters, aHeaders);
		return sendRequestAndReturnMap(oPost, bIsUseProxy, nTimeOut);
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
		oPost.setEntity(new UrlEncodedFormEntity(aPostParameters, Charset.forName("UTF-8")));

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
	protected static Map<String, Object> sendRequestAndReturnMap(final HttpUriRequest oHttpUriRequest, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		try
		{	
			final String strJson = sendRequestAndReturnText(oHttpUriRequest, bIsUseProxy, nTimeOut);
			return JsonUtils.json2Map(strJson);
		}
		catch (final Exception e)
		{
			String strMessage = CommonUtils.getExceptionMessage(e);
			strMessage = (strMessage.contains("Error executing query") ? strMessage : "Error executing query [" + oHttpUriRequest + "] [" + strMessage + "]");
			throw new Exception(strMessage); 
		}
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param oPost Запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	protected static List<Object> sendRequestAndReturnList(final HttpUriRequest oHttpUriRequest, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		try
		{	
			final String strJson = sendRequestAndReturnText(oHttpUriRequest, bIsUseProxy, nTimeOut);
			return JsonUtils.json2List(strJson);
		}
		catch (final Exception e)
		{
			String strMessage = CommonUtils.getExceptionMessage(e);
			strMessage = (strMessage.contains("Error executing query") ? strMessage : "Error executing query [" + oHttpUriRequest + "] [" + strMessage + "]");
			throw new Exception(strMessage); 
		}
	}
	
	/** Отправдяеи post запрос по указанному адресу
	 * @param oPost Запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	protected static <T extends Object> T sendRequestAndReturnObject(final HttpUriRequest oHttpUriRequest, final Boolean bIsUseProxy, final Class<T> oClass, final int nTimeOut) throws Exception
	{
		try
		{	
			final String strJson = sendRequestAndReturnText(oHttpUriRequest, bIsUseProxy, nTimeOut);
			return JsonUtils.fromJson(strJson, oClass);
		}
		catch (final Exception e)
		{
			String strMessage = CommonUtils.getExceptionMessage(e);
			strMessage = (strMessage.contains("Error executing query") ? strMessage : "Error executing query [" + oHttpUriRequest + "] [" + strMessage + "]");
			throw new Exception(strMessage); 
		}
	}

	/** Отправдяеи post запрос по указанному адресу
	 * @param oPost Запроса
	 * @return Ответ сервера в виде json 
	 * @throws Exception */
	public static String sendRequestAndReturnText(final HttpUriRequest oHttpUriRequest, final Boolean bIsUseProxy, final int nTimeOut) throws Exception
	{
		int nTryCount = getTryCount();
		
		try
		{	
			while (true)
			{
				final SSLContext oSSLContext = SSLContexts.custom().useTLS().build();
				final SSLConnectionSocketFactory oSSLConnectionSocketFactory = new SSLConnectionSocketFactory(
						oSSLContext,
						new String[]{"TLSv1.2"},   
						null,
						SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			
				final RequestConfig oConfig = getConfig(nTimeOut, bIsUseProxy);
				final CloseableHttpClient oClient = HttpClientBuilder.create()
						.setDefaultRequestConfig(oConfig)
						.setSSLSocketFactory(oSSLConnectionSocketFactory).build();	
				
				HttpResponse oResponse = getResponse(oHttpUriRequest, nTimeOut, oClient);
				if (null == oResponse)
					return null;
			
				final InputStream oSource = (InputStream) oResponse.getEntity().getContent();
				final StringWriter oWriter = new StringWriter();
				IOUtils.copy(oSource, oWriter, "UTF-8");
				final String strContent = oWriter.toString();

				if (oResponse.getStatusLine().getStatusCode() != 200 && oResponse.getStatusLine().getStatusCode() != 201 && oResponse.getStatusLine().getStatusCode() != 202)
				{
					nTryCount--;
					Thread.sleep(50);
					if (nTryCount > 0)
						continue;
					
					final String strMessage = oResponse.getStatusLine().toString(); //\r\nCause : " + strContent);
					throw new Exception(strMessage);
				}
				return strContent;
			}
		}
		catch (final Exception e)
		{
            throw new Exception("Error executing query [" + oHttpUriRequest + "] [" + CommonUtils.getExceptionMessage(e) + "]"); 
		}
	}

	static int getTryCount()
	{
		try
		{
			final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
			return ResourceUtils.getIntFromResource("stock.request.try_count", oStockExchange.getStockProperties(), 1);
		}
		catch(final Exception e)
		{
			return 1;
		}
	}

	static HttpResponse getResponse(final HttpUriRequest oHttpUriRequest, final int nTimeOut, final CloseableHttpClient oClient) throws Exception
	{
		if (nTimeOut > 10)
			return oClient.execute(oHttpUriRequest);
		
		final Semaphore oSemaphore = getSemaphore(oHttpUriRequest);
		try
		{
			oSemaphore.acquire();
			return oClient.execute(oHttpUriRequest);
		}
		finally
		{
			oSemaphore.release();
		}
	}
	
	public static Semaphore getSemaphore(final HttpUriRequest oHttpUriRequest)
	{
		return getSemaphore(oHttpUriRequest.getURI().getHost());
	}
	
	public static Semaphore getSemaphore(final String strHost)
	{
		if (!s_oAllSemaphores.containsKey(strHost))
			s_oAllSemaphores.put(strHost, new Semaphore(MAX_PARALEL_QUERY, true));

		return s_oAllSemaphores.get(strHost);
	}
	
	public static RequestConfig getConfig(final int nTimeOut, final Boolean bIsUseProxy)
	{
		Builder oConfigBuilder = RequestConfig.custom()
			.setConnectTimeout(nTimeOut * 1000)
			.setConnectionRequestTimeout(nTimeOut * 1000)
			.setSocketTimeout(nTimeOut * 1000);
		
		if (!bIsUseProxy)
			return oConfigBuilder.build();

		final String strProxyHost = ResourceUtils.getResource("proxy.host", CurrencyInformer.PROPERTIES_FILE_NAME);
		final int nProxyPort = ResourceUtils.getIntFromResource("proxy.port", CurrencyInformer.PROPERTIES_FILE_NAME, 0);
		
		if (StringUtils.isBlank(strProxyHost) || 0 == nProxyPort)
			return oConfigBuilder.build();

		final HttpHost oProxy = new HttpHost(strProxyHost, nProxyPort);
		oConfigBuilder = oConfigBuilder.setProxy(oProxy); 
		return oConfigBuilder.build();
	}
}

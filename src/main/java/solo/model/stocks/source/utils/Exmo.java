package solo.model.stocks.source.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import solo.CurrencyInformer;
import solo.model.request.RequestBlock;
import solo.model.request.RequestInfo;
import solo.utils.ResourceUtils;
import solo.utils.TraceUtils;

public class Exmo 
{
    private static final String EXMO_HOST = "api.exmo.com";
    private static final String HTTPS_API_EXMO_ROOT = "https://" + EXMO_HOST + "/v1/";
    private final int MAX_TRY_COUNT = 5;
    
	private static Long _nonce = 0L;
    private static Long _nonceNext = 0L;
    private String _key;
    private String _secret;
    
    static RequestBlock requests = new RequestBlock();

    public Exmo(String key, String secret) 
    {
        _key = key;
        _secret = secret;
    }
    
    public final String Request(String method, Map<String, String> arguments) 
    {
    	//final Semaphore oSemaphore = RequestUtils.getSemaphore(EXMO_HOST);
    	final RequestInfo oRequestInfo = requests.addRequest(method, MAX_TRY_COUNT);
		try
		{
			//oSemaphore.acquire();
			oRequestInfo.startExecute();
			final String strResult = execute(oRequestInfo, arguments);
			return finishRequst(oRequestInfo, strResult);
		}
		/*catch (InterruptedException e)
		{
			TraceUtils.writeError("Request fail [" + HTTPS_API_EXMO_ROOT + method + "]: " + e.toString());
			return finishRequst(oRequestInfo, null);
		}*/
		finally
		{
			//oSemaphore.release();
		}
    }

    public final String execute(final RequestInfo oRequestInfo, Map<String, String> arguments) 
    {
        if (arguments == null)
            arguments = new HashMap<>();
    	
    	Mac mac;
        try 
        {
        	final SecretKeySpec key = new SecretKeySpec(_secret.getBytes("UTF-8"), "HmacSHA512");
            mac = Mac.getInstance("HmacSHA512");
            mac.init(key);
        } 
        catch (Exception e) 
        {
            TraceUtils.writeError("No such algorithm or Invalid key exception: " + e.toString());
            return null;
        }
        
        final MediaType form = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
        final Proxy oProxy = getProxy();
        
        String strResult = null;
        while(oRequestInfo.tryMore()) 
        {
        	_nonce = System.nanoTime();
        	_nonce = _nonce++;
        	if (_nonce < _nonceNext)
        		_nonce = _nonceNext;
        	_nonceNext = _nonce + 1;
        	arguments.put("nonce", "" + _nonce);
	
        	String postData = StringUtils.EMPTY;
        	for (Map.Entry<String, String> stringStringEntry : arguments.entrySet()) 
        	{
        		if (postData.length() > 0) 
        			postData += "&";
        		postData += stringStringEntry.getKey() + "=" + stringStringEntry.getValue();
        	}
        	
        	String sign;
	        try 
	        {
	            sign = Hex.encodeHexString(mac.doFinal(postData.getBytes("UTF-8")));
	        } 
	        catch (UnsupportedEncodingException uee) 
	        {
	            TraceUtils.writeError("Unsupported encoding exception: " + uee.toString());
	            return null;
	        }
	
	        final Builder oBuilder = new OkHttpClient.Builder();
	        if (null != oProxy)
	        	oBuilder.proxy(oProxy);
	        OkHttpClient client = oBuilder.build();
	        final String strURL = HTTPS_API_EXMO_ROOT + oRequestInfo.getMethod();
	        try 
	        {
	            RequestBody body = RequestBody.create(form, postData);
	            Request request = new Request.Builder()
	                    .url(strURL)
	                    .addHeader("Key", _key)
	                    .addHeader("Sign", sign)
	                    .post(body)
	                    .build();
	            
	            Response response = client.newCall(request).execute();
	            strResult = response.body().string();
	            if (!strResult.contains("The nonce parameter is less or equal than what was used before"))
	            	return strResult;
	        } 
	        catch (IOException e) 
	        {
	            TraceUtils.writeError("Request fail [" + strURL + "]: " + e.toString());
	            return null;
	        }
        }
        
        return strResult;
    }
    
    static String finishRequst(final RequestInfo oRequestInfo, final String strResult)
    {
    	oRequestInfo.finish();
    	if (oRequestInfo.getDuration() > 1000)
    		TraceUtils.writeTrace(oRequestInfo + ". " + requests);
    	return strResult;
    }
	
	/** Устанавливаем прокси если нужно 
	 * @param bIsUseProxy Использовать прокси при выполнении запроса */
	public Proxy getProxy()
	{
		final String strProxyHost = ResourceUtils.getResource("proxy.host", CurrencyInformer.PROPERTIES_FILE_NAME);
		final int nProxyPort = ResourceUtils.getIntFromResource("proxy.port", CurrencyInformer.PROPERTIES_FILE_NAME, 0);
		
		if (StringUtils.isBlank(strProxyHost) || 0 == nProxyPort)
			return null;
		
		final SocketAddress oSocketAddress = new InetSocketAddress(strProxyHost, nProxyPort);
		return new Proxy(Type.HTTP, oSocketAddress);
	}
}
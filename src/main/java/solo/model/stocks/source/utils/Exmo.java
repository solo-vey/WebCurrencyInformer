package solo.model.stocks.source.utils;

import okhttp3.*;
import okhttp3.OkHttpClient.Builder;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import solo.CurrencyInformer;
import solo.utils.RequestUtils;
import solo.utils.ResourceUtils;
import solo.utils.TraceUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Exmo 
{
    private static final String EXMO_HOST = "api.exmo.com";
    private static final String HTTPS_API_EXMO_ROOT = "https://" + EXMO_HOST + "/v1/";
    
	private static Long _nonce = 0L;
    private static Long _nonceNext = 0L;
    private String _key;
    private String _secret;

    public Exmo(String key, String secret) 
    {
        _key = key;
        _secret = secret;
    }
    
    public final String Request(String method, Map<String, String> arguments) 
    {
    	final Semaphore oSemaphore = RequestUtils.getSemaphore(EXMO_HOST);
		try
		{
			oSemaphore.acquire();
			return execute(method, arguments);
		}
		catch (InterruptedException e)
		{
			TraceUtils.writeError("Request fail [" + HTTPS_API_EXMO_ROOT + method + "]: " + e.toString());
			return null;
		}
		finally
		{
			oSemaphore.release();
		}
    }

    public final String execute(String method, Map<String, String> arguments) 
    {
        Mac mac;
        SecretKeySpec key;
        String sign;

        if (arguments == null)   // If the user provided no arguments, just create an empty argument array.
            arguments = new HashMap<>();
        
        if (null == _nonce)
        	_nonce = 0L;
        
        if (null == _nonceNext)
        	_nonceNext = 0L;

        //synchronized (_nonce) 
        {
        	_nonce = System.nanoTime();
        	_nonce = _nonce++;
        	if (_nonce < _nonceNext)
        		_nonce = _nonceNext;
        	_nonceNext = _nonce + 1;
        	arguments.put("nonce", "" + _nonce);  // Add the dummy nonce.

        	String postData = StringUtils.EMPTY;

        	for (Map.Entry<String, String> stringStringEntry : arguments.entrySet()) 
        	{
        		if (postData.length() > 0) 
        			postData += "&";

        		postData += stringStringEntry.getKey() + "=" + stringStringEntry.getValue();
        	}

        	// Create a new secret key
        	try 
        	{
        		key = new SecretKeySpec(_secret.getBytes("UTF-8"), "HmacSHA512");
        	} 
        	catch (UnsupportedEncodingException uee) 
        	{
        		TraceUtils.writeError("Unsupported encoding exception: " + uee.toString());
        		return null;
        	}

	        // Create a new mac
	        try 
	        {
	            mac = Mac.getInstance("HmacSHA512");
	        } 
	        catch (NoSuchAlgorithmException nsae) 
	        {
	            TraceUtils.writeError("No such algorithm exception: " + nsae.toString());
	            return null;
	        }
	
	        // Init mac with key.
	        try 
	        {
	            mac.init(key);
	        } 
	        catch (InvalidKeyException ike) 
	        {
	            TraceUtils.writeError("Invalid key exception: " + ike.toString());
	            return null;
	        }
	
	
	        // Encode the post data by the secret and encode the result as base64.
	        try 
	        {
	            sign = Hex.encodeHexString(mac.doFinal(postData.getBytes("UTF-8")));
	        } 
	        catch (UnsupportedEncodingException uee) 
	        {
	            TraceUtils.writeError("Unsupported encoding exception: " + uee.toString());
	            return null;
	        }

	        // Now do the actual request
	        MediaType form = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
	
	        final Proxy oProxy = getProxy();
	        final Builder oBuilder = new OkHttpClient.Builder();
	        if (null != oProxy)
	        	oBuilder.proxy(oProxy);
	        OkHttpClient client = oBuilder.build();
	        final String strURL = HTTPS_API_EXMO_ROOT + method;
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
	            return response.body().string();
	        } 
	        catch (IOException e) 
	        {
	            TraceUtils.writeError("Request fail [" + strURL + "]: " + e.toString());
	            return null;  // An error occured...
	        }
        }
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
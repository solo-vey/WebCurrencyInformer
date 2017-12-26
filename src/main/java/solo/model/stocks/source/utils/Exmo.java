package solo.model.stocks.source.utils;

import okhttp3.*;
import okhttp3.OkHttpClient.Builder;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;

import solo.CurrencyInformer;
import ua.lz.ep.utils.ResourceUtils;

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

public class Exmo 
{
    private static long _nonce;
    private String _key;
    private String _secret;

    public Exmo(String key, String secret) 
    {
        _nonce = System.nanoTime();
        _key = key;
        _secret = secret;
    }

    public final String Request(String method, Map<String, String> arguments) 
    {
        Mac mac;
        SecretKeySpec key;
        String sign;

        if (arguments == null)   // If the user provided no arguments, just create an empty argument array.
            arguments = new HashMap<String, String>();

        arguments.put("nonce", "" + ++_nonce);  // Add the dummy nonce.

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
            System.err.println("Unsupported encoding exception: " + uee.toString());
            return null;
        }

        // Create a new mac
        try 
        {
            mac = Mac.getInstance("HmacSHA512");
        } 
        catch (NoSuchAlgorithmException nsae) 
        {
            System.err.println("No such algorithm exception: " + nsae.toString());
            return null;
        }

        // Init mac with key.
        try 
        {
            mac.init(key);
        } 
        catch (InvalidKeyException ike) 
        {
            System.err.println("Invalid key exception: " + ike.toString());
            return null;
        }


        // Encode the post data by the secret and encode the result as base64.
        try 
        {
            sign = Hex.encodeHexString(mac.doFinal(postData.getBytes("UTF-8")));
        } 
        catch (UnsupportedEncodingException uee) 
        {
            System.err.println("Unsupported encoding exception: " + uee.toString());
            return null;
        }

        // Now do the actual request
        MediaType form = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

        final Proxy oProxy = getProxy();
        final Builder oBuilder = new OkHttpClient.Builder();
        if (null != oProxy)
        	oBuilder.proxy(oProxy);
        OkHttpClient client = oBuilder.build();
        try 
        {
            RequestBody body = RequestBody.create(form, postData);
            Request request = new Request.Builder()
                    .url("https://api.exmo.com/v1/" + method)
                    .addHeader("Key", _key)
                    .addHeader("Sign", sign)
                    .post(body)
                    .build();
            
            Response response = client.newCall(request).execute();
            return response.body().string();
        } 
        catch (IOException e) 
        {
            System.err.println("Request fail: " + e.toString());
            return null;  // An error occured...
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
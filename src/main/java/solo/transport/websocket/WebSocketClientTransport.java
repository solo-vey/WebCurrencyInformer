package solo.transport.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.java_websocket.client.WebSocketClient;

import solo.model.stocks.item.RateInfo;
import solo.utils.TraceUtils;

public class WebSocketClientTransport
{
	private static final String HMAC_SHA512 = "HmacSHA512";

	public static WebSocketClient createPublicWs(final RateInfo oRateInfo) 
	{
	    try 
	    {
	    	final String strRate = oRateInfo.getCurrencyFrom().toString().toUpperCase() + "_" + oRateInfo.getCurrencyTo().toString().toUpperCase();
	        return startExmoClient("wss://ws-api.exmo.com:443/v1/public", new String[]{"{\"id\":1,\"method\":\"subscribe\",\"topics\":[\"spot/trades:" + strRate + "\",\"spot/order_book_snapshots:" + strRate + "\"]}"}, oRateInfo);
	    } 
	    catch (Exception e) 
	    {
	    	TraceUtils.writeError("Can't create websocket for [" + oRateInfo + "]", e);
	    	return null;
	    } 
	}
	
	public static WebSocketClient createProtectedWs(final String apiKey, final String secretKey)  
	{
	    try 
	    {
	        final long nonce = System.currentTimeMillis();
	
	        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
	        Mac mac = Mac.getInstance(HMAC_SHA512);
	        mac.init(keySpec);
	        byte[] macData = mac.doFinal((apiKey + nonce).getBytes(StandardCharsets.UTF_8));
	        String sign = Base64.getEncoder().encodeToString(macData);
	
	        String loginCommand = String.format("{\"id\":1,\"method\":\"login\",\"api_key\":\"%s\",\"sign\":\"%s\",\"nonce\":%d}", apiKey, sign, nonce);
	
	        return startExmoClient("wss://ws-api.exmo.com:443/v1/private", new String[]{
	                loginCommand,
	                "{\"id\":2,\"method\":\"subscribe\",\"topics\":[\"spot/orders\",\"spot/user_trades\"]}"
	        }, null);
	    } 
	    catch (Exception e) 
	    {
	    	TraceUtils.writeError("Can't create protected websocket", e);
	    	return null;
	    } 
	}
	
	public static WebSocketClient startExmoClient(String url, String[] initMessages, final RateInfo oRateInfo) throws URISyntaxException, InterruptedException 
	{
		WebSocketClient ws = null;
		try
		{
		    ws = newWsClient(url, oRateInfo);
		    ws.connectBlocking();
		    for (String message : initMessages) 
		    {
		        ws.send(message);
		        TraceUtils.writeTrace("sent: " + message);
		    }
		}
		catch (Exception e) 
	    {
	    	TraceUtils.writeError("Can't create websocket for [" + url + "]", e);
	    	if (ws != null) 
	        {
	            try 
	            {
	                ws.close();
	            } 
	            catch (Exception e1) { /***/ }
	        }
	    	
	    	throw e;
	    }
		
	    return ws;
	}
	
	private static WebSocketClient newWsClient(String url, final RateInfo oRateInfo) throws URISyntaxException 
	{
		if (null != oRateInfo)
			return new PublicWebSocketClient(new URI(url), oRateInfo);
		
		return new ProtectedWebSocketClient(new URI(url));
	}
}
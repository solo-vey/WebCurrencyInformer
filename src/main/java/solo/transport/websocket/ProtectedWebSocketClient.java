package solo.transport.websocket;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import solo.utils.TraceUtils;

public class ProtectedWebSocketClient extends WebSocketClient
{
	
	public ProtectedWebSocketClient(final URI uri) 
	{
		super(uri);
	}
	
	@Override public void onOpen(ServerHandshake serverHandshake) 
	{
	}
	
    @Override public void onMessage(String s) 
    {
        TraceUtils.writeTrace("read: " + s);
    }

    @Override public void onClose(int i, String s, boolean b) 
    {
        TraceUtils.writeTrace("close: " + i + " " + s);
    }

    @Override public void onError(Exception e) 
    {
        TraceUtils.writeTrace("error: " + e.toString());
	}
}
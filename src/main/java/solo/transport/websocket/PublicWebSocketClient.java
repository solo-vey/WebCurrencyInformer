package solo.transport.websocket;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.source.IStockSource;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.JsonUtils;
import solo.utils.TraceUtils;

public class PublicWebSocketClient extends WebSocketClient
{
	final RateInfo rateInfo;
	boolean isExecute = false;
	int nPassCount = 0;
	
	public PublicWebSocketClient(final URI uri, final RateInfo oRateInfo) 
	{
		super(uri);
		rateInfo = oRateInfo;
	}
	
	@Override public void onOpen(ServerHandshake serverHandshake) 
	{
	}
	
    @SuppressWarnings("unchecked")
	@Override public void onMessage(String s) 
    {
    	if (isExecute)
    	{ 	
    		nPassCount++;
        	if (nPassCount > 10)
            	TraceUtils.writeError("pass read [" + nPassCount + "]: " + rateInfo);
    		return;
    	}
    	
    	try
    	{  		
        	isExecute = true;
        	nPassCount = 0;
        	if (rateInfo.toString().equalsIgnoreCase("btcusd"))
        	{
        		//TraceUtils.writeError("!!!!!! START read: " + rateInfo);
        	}
//			TraceUtils.writeTrace("read: " + s);
			
			final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
			final IStockSource oStockSource = oStockExchange.getStockSource();
    		RateState oRateState = oStockSource.getCachedRateState(rateInfo);
    		if (null == oRateState)
    		{
    			oRateState = WorkerFactory.getStockSource().getRateState(rateInfo);
    			WorkerFactory.getStockTestSource().getRateState(rateInfo);
    			oStockExchange.getLastAnalysisResult().analyse(oRateState, oStockExchange, rateInfo);
    			
    			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(rateInfo);
    			final RateState oReverseRateState = makeReverseRateState(oRateState);
    			oStockExchange.getLastAnalysisResult().analyse(oReverseRateState, oStockExchange, oReverseRateInfo);
    			
    			oStockSource.setCachedRateState(rateInfo, oRateState);
    		}
    		
    		final Map<String, Object> oData = JsonUtils.json2Map(s);
    		if (null == oData.get("topic"))
    			return;
    		
    		final String strTopic = oData.get("topic").toString();
    		if (strTopic.startsWith("spot/order_book_snapshots"))
    		{
    			final Map<String, Object> oOrders = (Map<String, Object>) oData.get("data");
    			if (null != oOrders)
    			{	
	    			final List<Object> oAsks = (List<Object>) oOrders.get("ask");	
	    			if (null != oAsks)
	    			{
		    			final List<Order> oAsksOrders = new ArrayList<Order>(oAsks.size());
		    			for(final Object oAsk : oAsks)
		    			{
		    				final List<Object> oOrderData = (List<Object>) oAsk;
		    				final Order oOrder = new Order();
		    				oOrder.setPrice(new BigDecimal(oOrderData.get(0).toString()));
		    				oOrder.setVolume(new BigDecimal(oOrderData.get(1).toString()));
		    				oAsksOrders.add(oOrder);
		    			}
						oRateState.setAsksOrders(oAsksOrders);
	    			}
					
	    			final List<Object> oBids = (List<Object>) oOrders.get("bid");
	    			if (null != oBids)
	    			{
						final List<Order> oBidsOrders = new ArrayList<Order>(oBids.size());
		    			for(final Object oBid : oBids)
		    			{
		    				final List<Object> oOrderData = (List<Object>) oBid;
		    				final Order oOrder = new Order();
		    				oOrder.setPrice(new BigDecimal(oOrderData.get(0).toString()));
		    				oOrder.setVolume(new BigDecimal(oOrderData.get(1).toString()));
		    				oBidsOrders.add(oOrder);
		    			}
						oRateState.setBidsOrders(oBidsOrders);
	    			}
    			}
    		}
    		
    		if (strTopic.startsWith("spot/trades"))
    		{
    			final List<Object> oTrades = (List<Object>) oData.get("data");
    			if (null != oTrades)
    			{
	    			final List<Order> oTradeOrders = new ArrayList<Order>(oTrades.size());
	    			for(final Object oTrade : oTrades)
	    			{
	    				final Map<String, Object> oTradeData = (Map<String, Object>) oTrade;
	    				final Order oOrder = new Order();
	    				oOrder.setId(null != oTradeData.get("trade_id") ? oTradeData.get("trade_id").toString() : StringUtils.EMPTY);
	    				oOrder.setSide(null != oTradeData.get("type") ? oTradeData.get("type").toString() : StringUtils.EMPTY);
	    				oOrder.setPrice(null != oTradeData.get("price") ? new BigDecimal(oTradeData.get("price").toString()) : null);
	    				oOrder.setVolume(null != oTradeData.get("quantity") ? new BigDecimal(oTradeData.get("quantity").toString()) : null);
	    				oOrder.setSum(null != oTradeData.get("amount") ? new BigDecimal(oTradeData.get("amount").toString()) : null);
	    				oOrder.setCreated(null != oTradeData.get("date") ? new Date(1000L * (Integer)oTradeData.get("date")) : null);
	    				oTradeOrders.add(oOrder);
	    			}
	    			oRateState.setTrades(oTradeOrders);
    			}
    		}
			
			WorkerFactory.getStockTestSource().getRateState(rateInfo);
			oStockExchange.getLastAnalysisResult().analyse(oRateState, oStockExchange, rateInfo);
			
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(rateInfo);
			final RateState oReverseRateState = makeReverseRateState(oRateState);
			oStockExchange.getLastAnalysisResult().analyse(oReverseRateState, oStockExchange, oReverseRateInfo);
    	}
    	catch(final Exception e)
    	{
        	if (rateInfo.toString().equalsIgnoreCase("btcusd"))
        	{
        		//TraceUtils.writeError("!!!!!! END read: " + rateInfo);
        	}
        	
    		TraceUtils.writeError("Can't update rate state [" + rateInfo + "]", e);
    		System.err.println(s);
    	}
    	finally
    	{
    		isExecute = false;
    	}
    }

    @Override public void onClose(int i, String s, boolean b) 
    {
    	TraceUtils.writeTrace("close: " + i + " " + s);
    }

    @Override public void onError(Exception e) 
    {
    	TraceUtils.writeTrace("error: " + e.toString());
	}
    
    public static RateState makeReverseRateState(final RateState oRateState)
	{
		final RateState oReverseRateState = new RateState(RateInfo.getReverseRate(oRateState.getRateInfo()));
		for(final Order oOrder : oRateState.getBidsOrders())
			oReverseRateState.getAsksOrders().add(TradeUtils.makeReveseOrder(oOrder));
		for(final Order oOrder : oRateState.getAsksOrders())
			oReverseRateState.getBidsOrders().add(TradeUtils.makeReveseOrder(oOrder));
		for(final Order oOrder : oRateState.getTrades())
			oReverseRateState.getTrades().add(TradeUtils.makeReveseOrder(oOrder));
		return oReverseRateState;
	}
}
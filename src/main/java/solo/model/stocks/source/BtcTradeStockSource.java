package solo.model.stocks.source;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.StockUserInfo;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;
import solo.utils.RequestUtils;
import ua.lz.ep.utils.ResourceUtils;

public class BtcTradeStockSource extends BaseStockSource
{
	final protected String m_strBuyUrl;
	final protected String m_strSellUrl;
	final protected String m_strDealsUrl;
	final protected String m_strAuthUrl;
	final protected String m_strOrderStatusUrl;
	
	protected Integer m_nNonce;
	protected Integer m_nOutOrderId;
	protected boolean m_bIsAuthorized;
	
	public BtcTradeStockSource(final IStockExchange oStockExchange)
	{
		super(oStockExchange);
		m_strBuyUrl = ResourceUtils.getResource("buy.url", getStockExchange().getStockProperties());
		m_strSellUrl = ResourceUtils.getResource("sell.url", getStockExchange().getStockProperties());
		m_strDealsUrl = ResourceUtils.getResource("deals.url", getStockExchange().getStockProperties());

		m_strAuthUrl = ResourceUtils.getResource("auth.url", getStockExchange().getStockProperties());
		m_strOrderStatusUrl = ResourceUtils.getResource("order_status.url", getStockExchange().getStockProperties());
		
		m_aAllRates.add(new RateInfo(Currency.BTC, Currency.UAH));
		m_aAllRates.add(new RateInfo(Currency.ETH, Currency.UAH));

		restart();
	}
	
	@SuppressWarnings("unchecked")
	public RateState getRateState(final RateInfo oRateInfo) throws Exception
	{
		final RateState oRateState = super.getRateState(oRateInfo);
		
		final String strOrderBuyUrl = m_strBuyUrl.replace("#rate#", getRateIdentifier(oRateInfo));
		final Map<String, Object> oBuyOrders = RequestUtils.sendGetAndReturnMap(strOrderBuyUrl, true, RequestUtils.DEFAULT_TEMEOUT);
		final List<Order> oBidsOrders = convert2Orders((List<Object>) oBuyOrders.get("list"));
		oRateState.setBidsOrders(oBidsOrders);
		
		final String strOrderSellUrl = m_strSellUrl.replace("#rate#", getRateIdentifier(oRateInfo));
		final Map<String, Object> oSellOrders = RequestUtils.sendGetAndReturnMap(strOrderSellUrl, true, RequestUtils.DEFAULT_TEMEOUT);
		final List<Order> oAsksOrders = convert2Orders((List<Object>) oSellOrders.get("list"));
		oRateState.setAsksOrders(oAsksOrders);
		
		final String strDealsUrl = m_strDealsUrl.replace("#rate#", getRateIdentifier(oRateInfo));
		final List<Object> oInputTrades = RequestUtils.sendGetAndReturnList(strDealsUrl, true, RequestUtils.DEFAULT_TEMEOUT);
		final List<Order> oTrades = convert2Orders(oInputTrades);
		for(final Order oTradeOrder : oTrades)
		{
			if (oTradeOrder.getSide().equals(OrderSide.BUY))
				oRateState.getTrades().add(oTradeOrder);
		}
		
		return oRateState;
	}

	protected String getRateIdentifier(final RateInfo oRateInfo)
	{
		return oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + oRateInfo.getCurrencyTo().toString().toLowerCase();  
	}
	
	@SuppressWarnings("unchecked")
	@Override protected Order convert2Order(final Object oInputOrder)
	{
		final Map<String, Object> oMapOrder = (Map<String, Object>)oInputOrder;  
		final Order oOrder = new Order();
		if (null != oMapOrder.get("id"))
			oOrder.setId(oMapOrder.get("id").toString());

		if (null != oMapOrder.get("price"))
			oOrder.setPrice(MathUtils.fromString(oMapOrder.get("price").toString()));
		
		if (null != oMapOrder.get("currency_trade"))
			oOrder.setVolume(MathUtils.fromString(oMapOrder.get("currency_trade").toString()));
		
		if (null != oMapOrder.get("amnt_trade"))
			oOrder.setVolume(MathUtils.fromString(oMapOrder.get("amnt_trade").toString()));

		if (null != oMapOrder.get("type"))
			oOrder.setSide(oMapOrder.get("type").toString());
		
//		if (null != oMapOrder.get("pub_date"))
//			oOrder.setCreated(oMapOrder.get("pub_date").toString(), "yyyy-MM-dd HH:mm:ss");
		
		return oOrder;
	}
	
	@Override public void restart()
	{
		m_bIsAuthorized = false;
		m_nNonce = 1;
		m_nOutOrderId = (int)(Math.random() * 1000000);
	}
	
	@Override public StockUserInfo getUserInfo(final RateInfo oRateInfo) throws Exception
	{
		final StockUserInfo oUserInfo = super.getUserInfo(oRateInfo);
		authUser();
		setUserMoney(oUserInfo);
		setUserOrders(oUserInfo, oRateInfo);
		return oUserInfo;
	}
	
	public void authUser()
	{
		if (m_bIsAuthorized)
			return;

		try
		{
			final Map<String, Object> oResult = sendPost(m_strAuthUrl, null);
			m_bIsAuthorized = (null != oResult.get("status") && oResult.get("status").toString().equalsIgnoreCase("true"));
		}
		catch(final Exception e) {}
	}
	
	@SuppressWarnings("unchecked")
	public void setUserMoney(final StockUserInfo oUserInfo)
	{
		try
		{
			final Map<String, Object> oMoneyInfo = sendPost(m_strMoneyUrl, null);
			final List<Map<String, Object>> oAccounts = (List<Map<String, Object>>) oMoneyInfo.get("accounts");
			for(final Map<String, Object> oAccount : oAccounts)
			{
				final BigDecimal nBalance = MathUtils.fromString(oAccount.get("balance").toString());
				if (nBalance.compareTo(BigDecimal.ZERO) == 0)
					continue;
				
				final String strCurrency = oAccount.get("currency").toString();
				final Currency oCurrency = Currency.valueOf(strCurrency.toUpperCase());
				oUserInfo.getMoney().put(oCurrency, new CurrencyAmount(nBalance, BigDecimal.ZERO)); 
			}
		}
		catch(final Exception e) {}
	}
	
	@SuppressWarnings("unchecked")
	public void setUserOrders(final StockUserInfo oUserInfo, final RateInfo oRequestRateInfo)
	{
		try
		{
			for(final RateInfo oRateInfo : getRates())
			{
				if (null != oRequestRateInfo && !oRequestRateInfo.equals(oRateInfo))
					continue;
				
				final String strMarket = getRateIdentifier(oRateInfo);
				final Map<String, Object> oOrdersInfo = sendPost(m_strMyOrdersUrl.replace("#market#", strMarket), null);
				final List<Object> oOrders = (List<Object>) oOrdersInfo.get("your_open_orders");
				
				for(final Object oOrderInfo : oOrders)
				{
					final Order oOrder = convert2Order(oOrderInfo);
					oOrder.setState("wait");
					oUserInfo.addOrder(oRateInfo, oOrder); 
				}
			}
		}
		catch(final Exception e) {}
	}
	
	@Override public Order addOrder(final OrderSide oSide, final RateInfo oRateInfo, final BigDecimal nVolume, final BigDecimal nPrice)
	{
		try
		{
			checkOrderParameters(oSide, oRateInfo, nPrice);
			
			authUser();
			super.addOrder(oSide, oRateInfo, nVolume, nPrice);
			
			final Map<String, String> aParameters = new HashMap<String, String>();
			aParameters.put("side", oSide.toString().toLowerCase());
			aParameters.put("count", nVolume.toString());
			aParameters.put("currency", oRateInfo.getCurrencyFrom().toString());
			aParameters.put("currency1", oRateInfo.getCurrencyTo().toString());
			aParameters.put("price", nPrice.toString());

			final String strMarket = getRateIdentifier(oRateInfo);
			final String strAddOrder = m_strAddOrderUrl.replace("#side#", oSide.toString().toLowerCase()).replace("#rate#", strMarket);

			final Map<String, Object> oOrder = sendPost(strAddOrder, aParameters);
			if (!"true".equals(oOrder.get("status").toString()))
			{
				final Object oDescription = oOrder.get("description"); 
				return new Order(Order.CANCEL, (null != oDescription ? oDescription.toString() : StringUtils.EMPTY));
			}
			
			final String strOrderId = oOrder.get("order_id").toString();
			return getOrder(strOrderId, oRateInfo);
		}
		catch(final Exception e)
		{
			return new Order(Order.EXCEPTION, e.getMessage());
		}
	}
	
	@Override public Order getOrder(final String strOrderId, final RateInfo oRateInfo)
	{
		try
		{
			authUser();
			super.getOrder(strOrderId, oRateInfo);
			
			final StockUserInfo oUserInfo = super.getUserInfo(oRateInfo);
			setUserOrders(oUserInfo, oRateInfo);
			Order oThisOrder = new Order();
			for(final Order oOrder : oUserInfo.getOrders(oRateInfo))
			{
				if (oOrder.getId().equalsIgnoreCase(strOrderId))
				{
					oThisOrder = oOrder;
					break;
				}
			}
			
			final Map<String, Object> oTradeOrderInfo = sendPost(m_strOrderStatusUrl.replace("#id#", strOrderId), null);
			addOrderTradeInfo(oThisOrder, oTradeOrderInfo);
			return oThisOrder;
		}
		catch(final Exception e)
		{
			return new Order(Order.EXCEPTION, e.getMessage());
		}
	}
	
	protected void addOrderTradeInfo(final Order oOrder, final Map<String, Object> oTradeOrderInfo)
	{
		if (null != oTradeOrderInfo.get("id"))
			oOrder.setId(oTradeOrderInfo.get("id").toString());

		if (null != oTradeOrderInfo.get("type"))
			oOrder.setSide(oTradeOrderInfo.get("type").toString());
		
		if (oOrder.getSide().equals(OrderSide.BUY) && null != oTradeOrderInfo.get("sum2"))
		{
			oOrder.setVolume(MathUtils.fromString(oTradeOrderInfo.get("sum2").toString()));
			if (null != oTradeOrderInfo.get("sum1"))
			{
				final BigDecimal oSum = MathUtils.fromString(oTradeOrderInfo.get("sum1").toString());
				oOrder.setPrice(MathUtils.getRoundedBigDecimal(oSum.doubleValue() / oOrder.getVolume().doubleValue(), 0));
			}
		}

		if (oOrder.getSide().equals(OrderSide.SELL) && null != oTradeOrderInfo.get("sum1"))
		{
			oOrder.setVolume(MathUtils.fromString(oTradeOrderInfo.get("sum1").toString()));
			if (null != oTradeOrderInfo.get("sum2"))
			{
				final BigDecimal oSum = MathUtils.fromString(oTradeOrderInfo.get("sum2").toString());
				oOrder.setPrice(MathUtils.getRoundedBigDecimal(oSum.doubleValue() / oOrder.getVolume().doubleValue(), 0));
			}
		}

		if (null != oTradeOrderInfo.get("status"))
			oOrder.setState(oTradeOrderInfo.get("status").toString());
	}

	@Override public Order removeOrder(final String strOrderId)
	{
		authUser();
		super.removeOrder(strOrderId);
		final Order oOrder = getOrder(strOrderId, null);
		try
		{
			final Map<String, Object> oResult = sendPost(m_strRemoveOrderUrl.replace("#id#", strOrderId), null);
			if (!"true".equals(oResult.get("status")))
				return oOrder;
		}
		catch(final Exception e)
		{
			return new Order(Order.EXCEPTION, e.getMessage());
		}
			
		oOrder.setState(Order.CANCEL);	
		return oOrder;
	}
	
	@Override public List<Order> getTrades(RateInfo m_oRateInfo, final int nPage, final int nCount)
	{
		return new LinkedList<Order>();
	}
	
	public Map<String, Object> sendPost(final String strUrl, Map<String, String> aParameters) throws Exception
	{
		aParameters = (null == aParameters ? new HashMap<String, String>() : aParameters);
		
		aParameters.put("out_order_id", m_nOutOrderId.toString());
		aParameters.put("nonce", m_nNonce.toString());
		
		final Map<String, String> aHeaders = new HashMap<String, String>();
		aHeaders.put("public-key", m_strPublicKey);
		aHeaders.put("api-sign", signatureUrl(aParameters));
		return RequestUtils.sendPostAndReturnJson(strUrl, aParameters, aHeaders, true, RequestUtils.DEFAULT_TEMEOUT);
	}

	public String signatureUrl(final Map<String, String> aParameters) throws Exception
	{
		m_nNonce++;
		
		final ArrayList<NameValuePair> aPostParameters = new ArrayList<NameValuePair>();
		for(final Entry<String, String> oParameter : aParameters.entrySet())
			aPostParameters.add(new BasicNameValuePair(oParameter.getKey(), oParameter.getValue()));
		final UrlEncodedFormEntity oEncodedFormEntity = new UrlEncodedFormEntity(aPostParameters);
		final InputStream oStream = oEncodedFormEntity.getContent();
		final byte[] aBuffer = new byte[(int) oEncodedFormEntity.getContentLength()];
		oStream.read(aBuffer, 0, aBuffer.length);
		final String strData = new String(aBuffer) + m_strSecretKey;
		return CommonUtils.encodeSha256(strData);
	}
}

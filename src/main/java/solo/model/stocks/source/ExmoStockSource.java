package solo.model.stocks.source;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.source.utils.Exmo;
import solo.utils.MathUtils;
import solo.utils.RequestUtils;
import ua.lz.ep.utils.JsonUtils;
import ua.lz.ep.utils.ResourceUtils;

public class ExmoStockSource extends BaseStockSource
{
	final protected String m_strOrdersUrl;
	final protected String m_strTradesUrl;
	
	public ExmoStockSource(final IStockExchange oStockExchange)
	{
		super(oStockExchange);
		m_strOrdersUrl = ResourceUtils.getResource("orders.url", getStockExchange().getStockProperties());
		m_strTradesUrl = ResourceUtils.getResource("deals.url", getStockExchange().getStockProperties());
		
		registerRate(new RateInfo(Currency.ETH, Currency.UAH));
		registerRate(new RateInfo(Currency.ETH, Currency.RUB));
//		registerRate(new RateInfo(Currency.ETH, Currency.USD));
//		registerRate(new RateInfo(Currency.ETH, Currency.EUR));

		registerRate(new RateInfo(Currency.BTC, Currency.UAH));
		registerRate(new RateInfo(Currency.BTC, Currency.RUB));
		
//		registerRate(new RateInfo(Currency.WAVES, Currency.RUB));

		registerRate(new RateInfo(Currency.USD, Currency.RUB));
	}
	
	@SuppressWarnings("unchecked")
	public RateState getRateState(final RateInfo oRateInfo) throws Exception
	{
		final RateState oRateState = super.getRateState(oRateInfo);
		
		final String strMarket = getRateIdentifier(oRateInfo); 
		final String strOrderBookUrl = m_strOrdersUrl.replace("#rate#", strMarket);
		final Map<String, Object> oAllOrders = RequestUtils.sendGetAndReturnMap(strOrderBookUrl, true);
		final Map<String, Object> oRateOrders = (Map<String, Object>) oAllOrders.get(strMarket);
		final List<Order> oAsksOrders = convert2Orders((List<Object>) oRateOrders.get("ask"));
		final List<Order> oBidsOrders = convert2Orders((List<Object>) oRateOrders.get("bid"));
		oRateState.setAsksOrders(oAsksOrders);
		oRateState.setBidsOrders(oBidsOrders);
		
		final String strTradesUrl = m_strTradesUrl.replace("#rate#", strMarket);
		final Map<String, Object> oTrades = RequestUtils.sendGetAndReturnMap(strTradesUrl, true);
		final List<Object> oRateTrades = (List<Object>) oTrades.get(strMarket);
		final List<Order> oTradeOrders = convert2Orders(oRateTrades);
		oRateState.setTrades(oTradeOrders);
		
		return oRateState;
	}

	protected String getRateIdentifier(final RateInfo oRateInfo)
	{
		return oRateInfo.getCurrencyFrom().toString().toUpperCase() + "_" + oRateInfo.getCurrencyTo().toString().toUpperCase();  
	}
	
	@SuppressWarnings("unchecked")
	@Override protected Order convert2Order(final Object oInputOrder)
	{
		if (null == oInputOrder)
			return null;
		
		final Order oOrder = new Order();
		
		if (oInputOrder instanceof List<?>)
		{
			final List<Object> oListParams = (List<Object>)oInputOrder;  
			oOrder.setPrice(MathUtils.fromString(oListParams.get(0).toString()));
			oOrder.setState(Order.WAIT);
			oOrder.setVolume(MathUtils.fromString(oListParams.get(1).toString()));
		}
		
		if (oInputOrder instanceof Map<?, ?>)
		{
			final Map<String, Object> oMapOrder = (Map<String, Object>)oInputOrder;  
			if (null != oMapOrder.get("trade_id"))
				oOrder.setId(oMapOrder.get("trade_id").toString());
			else
			if (null != oMapOrder.get("order_id"))
				oOrder.setId(oMapOrder.get("order_id").toString());

			if (null != oMapOrder.get("type"))
				oOrder.setSide(oMapOrder.get("type").toString());

			if (null != oMapOrder.get("price"))
				oOrder.setPrice(MathUtils.fromString(oMapOrder.get("price").toString()));
			else
			if (null != oMapOrder.get("out_amount") && null != oMapOrder.get("in_amount"))
			{
				final BigDecimal nOutAmount = MathUtils.fromString(oMapOrder.get("out_amount").toString());
				final BigDecimal nInAmount = MathUtils.fromString(oMapOrder.get("in_amount").toString());
				
				if (nOutAmount.compareTo(BigDecimal.ZERO) > 0 && nInAmount.compareTo(BigDecimal.ZERO) > 0)
				{
					if (oOrder.getSide().equals(OrderSide.BUY))
						oOrder.setPrice(MathUtils.getBigDecimal(nOutAmount.doubleValue() / nInAmount.doubleValue(), TradeUtils.DEFAULT_VOLUME_PRECISION));
					else
						oOrder.setPrice(MathUtils.getBigDecimal(nInAmount.doubleValue() / nOutAmount.doubleValue(), TradeUtils.DEFAULT_VOLUME_PRECISION));
				}
			}
			
			if (null != oMapOrder.get("quantity"))
				oOrder.setVolume(MathUtils.fromString(oMapOrder.get("quantity").toString()));
			else
			if (null != oMapOrder.get("in_amount") && oOrder.getSide().equals(OrderSide.BUY))
				oOrder.setVolume(MathUtils.fromString(oMapOrder.get("in_amount").toString()));
			else
			if (null != oMapOrder.get("out_amount") && oOrder.getSide().equals(OrderSide.SELL))
					oOrder.setVolume(MathUtils.fromString(oMapOrder.get("out_amount").toString()));
			
			if (null != oMapOrder.get("date"))
			{
				final Integer nDate = (Integer)oMapOrder.get("date");
				oOrder.setCreated(new Date(((long)nDate) * 1000));
			}

			if (null != oMapOrder.get("created"))
			{
				final String strDate = oMapOrder.get("created").toString();
				final Long nDate = Long.decode(strDate);
				oOrder.setCreated(new Date(((long)nDate) * 1000));
			}
			
			oOrder.setState(Order.WAIT);
		}
		
		return oOrder;
	}
	
	@Override public StockUserInfo getUserInfo(final RateInfo oRateInfo)
	{
		final StockUserInfo oUserInfo = super.getUserInfo(oRateInfo);
		setUserMoney(oUserInfo);
		setUserOrders(oUserInfo, oRateInfo);
		return oUserInfo;
	}
	
	@SuppressWarnings("unchecked")
	public void setUserMoney(final StockUserInfo oUserInfo)
	{
		try
		{
			final Exmo oUserInfoRequest = new Exmo(m_strPublicKey, m_strSecretKey);
			final String oUserInfoJson = oUserInfoRequest.Request("user_info", null);
			final Map<String, Object> oUserInfoData = JsonUtils.json2Map(oUserInfoJson);
			final Map<String, Object> oUserBalances = (Map<String, Object>) oUserInfoData.get("balances");
			final Map<String, Object> oUserReserved = (Map<String, Object>) oUserInfoData.get("reserved");

			for(final Entry<String, Object> oCurrencyBalance : oUserBalances.entrySet())
			{
				final BigDecimal nBalance = MathUtils.fromString(oCurrencyBalance.getValue().toString());
				if (nBalance.compareTo(BigDecimal.ZERO) == 0)
					continue;
				
				final String strCurrency = oCurrencyBalance.getKey().toString();
				final Currency oCurrency = Currency.valueOf(strCurrency.toUpperCase());
				final BigDecimal nReserved = (oUserReserved.containsKey(strCurrency) ? MathUtils.fromString(oUserReserved.get(strCurrency).toString()) : BigDecimal.ZERO);
				oUserInfo.getMoney().put(oCurrency, new CurrencyAmount(nBalance, nReserved)); 
			}
		}
		catch(final Exception e) {}
	}

	@SuppressWarnings("unchecked")
	public void setUserOrders(final StockUserInfo oUserInfo, final RateInfo oRequestRateInfo)
	{
		try
		{
			final Exmo oUserInfoRequest = new Exmo(m_strPublicKey, m_strSecretKey);
			final String oUserInfoJson = oUserInfoRequest.Request("user_open_orders", null);
			final Map<String, Object> oAllOrdersData = JsonUtils.json2Map(oUserInfoJson);
			
			for(final RateInfo oRateInfo : getRates())
			{
				final String strMarket = getRateIdentifier(oRateInfo);
				final List<Object> oRateOrders = (List<Object>) oAllOrdersData.get(strMarket);
				if (null == oRateOrders)
					continue;
				
				for(final Object oOrderInfo : oRateOrders)
				{
					final Order oOrder = convert2Order(oOrderInfo);
					oOrder.setState("wait");
					oUserInfo.addOrder(oRateInfo, oOrder); 
				}
			}
		}
		catch(final Exception e) {}
	}
	
	@SuppressWarnings("serial")
	@Override public Order getOrder(final String strOrderId, final RateInfo oRateInfo)
	{
		super.getOrder(strOrderId, oRateInfo);
		
		final StockUserInfo oUserInfo = super.getUserInfo(oRateInfo);
		setUserOrders(oUserInfo, oRateInfo);
		for(final Order oOrder : oUserInfo.getOrders(oRateInfo))
		{
			if (oOrder.getId().equalsIgnoreCase(strOrderId))
				return oOrder;
		}

		try
		{
			final Exmo oUserInfoRequest = new Exmo(m_strPublicKey, m_strSecretKey);
			final String oOrderJson = oUserInfoRequest.Request("order_trades", new HashMap<String, String>() {{
				put("order_id", strOrderId);
			}});
			final Map<String, Object> oOrderData = JsonUtils.json2Map(oOrderJson);
			if (null != oOrderData.get("result") && "false".equals(oOrderData.get("result").toString()))
				return new Order(strOrderId, Order.ERROR, oOrderData.get("error").toString());

			final Order oOrder = convert2Order(oOrderData);
			oOrder.setState(Order.DONE);
			oOrder.setId(strOrderId);
			return oOrder;
		}
		catch(final Exception e)
		{
			return new Order(Order.ERROR, e.getMessage());
		}
	}
	
	@Override public Order addOrder(final OrderSide oSide, final RateInfo oRateInfo, final BigDecimal nVolume, final BigDecimal nPrice)
	{
		try
		{
			checkOrderParameters(oSide, oRateInfo, nPrice);
			
			super.addOrder(oSide, oRateInfo, nVolume, nPrice);
			
			final Map<String, String> aParameters = new HashMap<String, String>();
			aParameters.put("type", oSide.toString().toLowerCase());
			aParameters.put("quantity", nVolume.toString());
			aParameters.put("price", nPrice.toString());
			aParameters.put("pair", getRateIdentifier(oRateInfo));

			final Exmo oUserInfoRequest = new Exmo(m_strPublicKey, m_strSecretKey);
			final String oOrderJson = oUserInfoRequest.Request("order_create", aParameters);
			final Map<String, Object> oOrderData = JsonUtils.json2Map(oOrderJson);
			if ("true".equals(oOrderData.get("result").toString()))
			{
				final String strOrderId = oOrderData.get("order_id").toString(); 
				final Order oOrder = getOrder(strOrderId, oRateInfo);
				return oOrder;
			}
			
			return new Order(Order.ERROR, oOrderData.get("error").toString());
		}
		catch(final Exception e)
		{
			return new Order(Order.ERROR, e.getMessage());
		}
	}
	
	@SuppressWarnings("serial")
	@Override public Order removeOrder(final String strOrderId)
	{
		super.removeOrder(strOrderId);
		
		try
		{
			final Exmo oUserInfoRequest = new Exmo(m_strPublicKey, m_strSecretKey);
			final String oOrderJson = oUserInfoRequest.Request("order_cancel", new HashMap<String, String>() {{
				put("order_id", strOrderId);
			}});
			
			final Map<String, Object> oOrderData = JsonUtils.json2Map(oOrderJson);
			if (null != oOrderData.get("result") && "true".equals(oOrderData.get("result").toString()))
				return new Order(strOrderId, Order.CANCEL, StringUtils.EMPTY);
			
			return new Order(Order.ERROR, oOrderData.get("error").toString());
		}
		catch(final Exception e)
		{
			return new Order(Order.ERROR, e.getMessage());
		}
	}
}

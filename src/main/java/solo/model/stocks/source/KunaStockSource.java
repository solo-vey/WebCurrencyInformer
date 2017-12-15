package solo.model.stocks.source;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

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

public class KunaStockSource extends BaseStockSource
{
	final protected String m_strOrdersUrl;
	final protected String m_strTradesUrl;
	final protected String m_strMyTradesUrl;
	
	protected Long m_nTimeDelta;
	
	public KunaStockSource(final IStockExchange oStockExchange)
	{
		super(oStockExchange);
		m_strOrdersUrl = ResourceUtils.getResource("orders.url", getStockExchange().getStockProperties());
		m_strTradesUrl = ResourceUtils.getResource("trades.url", getStockExchange().getStockProperties());
		m_strMyTradesUrl = ResourceUtils.getResource("my_trades.url", getStockExchange().getStockProperties());
		
		registerRate(new RateInfo(Currency.BTC, Currency.UAH));
		registerRate(new RateInfo(Currency.ETH, Currency.UAH));
		registerRate(new RateInfo(Currency.WAVES, Currency.UAH));
	}
	
	@SuppressWarnings("unchecked")
	protected RateState getRateState(final RateInfo oRateInfo) throws Exception
	{
		final RateState oRateState = super.getRateState(oRateInfo);
		
		final String strOrderBookUrl = m_strOrdersUrl.replace("#rate#", getRateIdentifier(oRateInfo));
		final Map<String, Object> oAllOrders = RequestUtils.sendGetAndReturnMap(strOrderBookUrl, true);
		final List<Order> oAsksOrders = convert2Orders((List<Object>) oAllOrders.get("asks"));
		final List<Order> oBidsOrders = convert2Orders((List<Object>) oAllOrders.get("bids"));
		oRateState.setAsksOrders(oAsksOrders);
		oRateState.setBidsOrders(oBidsOrders);
		
		final String strTradesUrl = m_strTradesUrl.replace("#rate#", getRateIdentifier(oRateInfo));
		final List<Object> oInputTrades = RequestUtils.sendGetAndReturnList(strTradesUrl, true);
		final List<Order> oTrades = convert2Orders(oInputTrades);
		oRateState.setTrades(oTrades);
		
		return oRateState;
	}

	protected String getRateIdentifier(final RateInfo oRateInfo)
	{
		return oRateInfo.getCurrencyFrom().toString().toLowerCase() + oRateInfo.getCurrencyTo().toString().toLowerCase();  
	}
	
	@SuppressWarnings("unchecked")
	@Override protected Order convert2Order(final Object oInputOrder)
	{
		if (null == oInputOrder)
			return null;
		
		final Map<String, Object> oMapOrder = (Map<String, Object>)oInputOrder;  
		final Order oOrder = new Order();
		if (null != oMapOrder.get("order_id"))
			oOrder.setId(oMapOrder.get("order_id").toString());
		else 
			if (null != oMapOrder.get("id"))
				oOrder.setId(oMapOrder.get("id").toString());

		if (null != oMapOrder.get("price"))
			oOrder.setPrice(MathUtils.fromString(oMapOrder.get("price").toString()));
		
		if (null != oMapOrder.get("state"))
			oOrder.setState(oMapOrder.get("state").toString());

		if (null != oMapOrder.get("side"))
			oOrder.setSide(oMapOrder.get("side").toString());
		
		if (null != oMapOrder.get("remaining_volume"))
			oOrder.setVolume(MathUtils.fromString(oMapOrder.get("remaining_volume").toString()));
		else
			oOrder.setVolume(MathUtils.fromString(oMapOrder.get("volume").toString()));
		
		if (null != oMapOrder.get("created_at"))
			oOrder.setCreated(oMapOrder.get("created_at").toString().replace("T", " ").replace("Z", ""), "yyyy-MM-hh HH:mm:ss");
		
		return oOrder;
	}
	
	@Override public void restart()
	{
		m_nTimeDelta = null;
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
			final Map<String, Object> oMoneyInfo = RequestUtils.sendGetAndReturnMap(signatureUrl(m_strMoneyUrl, "GET"), true);
			
			final List<Map<String, Object>> oAccounts = (List<Map<String, Object>>) oMoneyInfo.get("accounts");
			for(final Map<String, Object> oAccount : oAccounts)
			{
				final BigDecimal nBalance = MathUtils.fromString(oAccount.get("balance").toString());
				final BigDecimal nLocked = MathUtils.fromString(oAccount.get("locked").toString());
				if (nBalance.compareTo(BigDecimal.ZERO) == 0 && nLocked.compareTo(BigDecimal.ZERO) == 0)
					continue;
				
				final String strCurrency = oAccount.get("currency").toString();
				final Currency oCurrency = Currency.valueOf(strCurrency.toUpperCase());
				oUserInfo.getMoney().put(oCurrency, new CurrencyAmount(nBalance, nLocked)); 
			}
		}
		catch(final Exception e) {}
	}
	
	public void setUserOrders(final StockUserInfo oUserInfo, final RateInfo oRequestRateInfo)
	{
		try
		{
			for(final RateInfo oRateInfo : getRates())
			{
				if (null != oRequestRateInfo && !oRequestRateInfo.equals(oRateInfo))
					continue;
				
				final String strMarket = getRateIdentifier(oRateInfo);
				final List<Object> oOrdersInfo = RequestUtils.sendGetAndReturnList(signatureUrl(m_strMyOrdersUrl.replace("#market#", strMarket), "GET"), true);
				
				for(final Object oOrderInfo : oOrdersInfo)
				{
					final Order oOrder = convert2Order(oOrderInfo); 
					oUserInfo.addOrder(oRateInfo, oOrder); 
				}
			}
		} 
		catch(final Exception e) {}
	}
	
	@Override public Order getOrder(final String strOrderId, final RateInfo oRateInfo)
	{
		final StockUserInfo oUserInfo = getUserInfo(oRateInfo);
		final List<Order> aOrders = oUserInfo.getOrders().get(oRateInfo);
		if (null != aOrders)
		{
			for(final Order oOrder : aOrders)
			{
				if (oOrder.getId().equalsIgnoreCase(strOrderId))
					return oOrder;
			}
		}
		
		return findOrderInTrades(strOrderId, oRateInfo);
	}
	
	public Order findOrderInTrades(final String strOrderId, final RateInfo oRateInfo)
	{
		try
		{
			final String strMarket = getRateIdentifier(oRateInfo);
			final List<Object> oOrdersInfo = RequestUtils.sendGetAndReturnList(signatureUrl(m_strMyTradesUrl.replace("#market#", strMarket), "GET"), true);
			for(final Object oOrderInfo : oOrdersInfo)
			{
				final Order oOrder = convert2Order(oOrderInfo);
				if (oOrder.getId().equalsIgnoreCase(strOrderId))
				{
					oOrder.setState("done");
					return oOrder;
				}
			}
		}
		catch (Exception e) { }
		
		
		return new Order(strOrderId, "cancel", "Order is absent");
	}
	
	@Override public Order addOrder(final OrderSide oSide, final RateInfo oRateInfo, final BigDecimal nVolume, final BigDecimal nPrice)
	{
		final String strError = checkOrderParameters(oSide, oRateInfo, nPrice);
		if (StringUtils.isNotBlank(strError))
			return new Order("cancel", strError);
		
		final Map<String, String> aParameters = new HashMap<String, String>();
		aParameters.put("side", oSide.toString().toLowerCase());
		aParameters.put("volume", nVolume.toString());
		aParameters.put("market", getRateIdentifier(oRateInfo));
		aParameters.put("price", nPrice.toString());
		final String strAddOrder = m_strAddOrderUrl.replace("#side#", oSide.toString().toLowerCase()).replace("#volume#", nVolume.toString())
													.replace("#market#", getRateIdentifier(oRateInfo)).replace("#price#", nPrice.toString());
		try
		{
			Map<String, Object> oOrderInfo = RequestUtils.sendPostAndReturnJson(signatureUrl(strAddOrder, "POST"), aParameters, true);
			return convert2Order(oOrderInfo);
		}
		catch (Exception e)
		{
			return new Order("cancel", e.getMessage());
		}
	}
	
	@Override public Order removeOrder(final String strOrderId)
	{
		super.removeOrder(strOrderId);
		
		final Map<String, String> aParameters = new HashMap<String, String>();
		aParameters.put("id", strOrderId);
		try
		{
			final Map<String, Object> oOrder = RequestUtils.sendPostAndReturnJson(signatureUrl(m_strRemoveOrderUrl.replace("#id#", strOrderId), "POST"), aParameters, true);
			return convert2Order(oOrder);
		}
		catch (Exception e)
		{
			return new Order("cancel", e.getMessage());
		}
	}
	
	public String signatureUrl(final String strUrl, final String strQueryType) throws Exception
	{
		final String strPrepareUrl = strUrl.replace("#access_key#", m_strPublicKey).replace("#tonce#", getTonce());
		final String strSignature = CommonUtils.encodeSha256HMAC(m_strSecretKey, strQueryType.toUpperCase() + "|" + strPrepareUrl.replace("https://kuna.io", "").replace("?", "|"));
		return strPrepareUrl + "&signature=" + strSignature;
	}

	public String getTonce() throws Exception
	{
		if (null == m_nTimeDelta)
		{
			final Long nStockTime = Long.parseLong(RequestUtils.sendGet(m_strTimeUrl, true));
			m_nTimeDelta = (new Date()).getTime() - nStockTime * 1000; 
			return nStockTime.toString() + "123";
		}
		else
		{
			Long oTimeNow = (new Date()).getTime();
			oTimeNow -= m_nTimeDelta;
			return oTimeNow.toString();
		}
	}
}

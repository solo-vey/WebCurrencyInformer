package solo.model.stocks.source;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
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
	
	protected Long m_nTimeDelta;
	
	public KunaStockSource(final IStockExchange oStockExchange)
	{
		super(oStockExchange);
		m_strOrdersUrl = ResourceUtils.getResource("orders.url", getStockExchange().getStockProperties());
		m_strTradesUrl = ResourceUtils.getResource("trades.url", getStockExchange().getStockProperties());
		
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
		if (oMapOrder.containsKey("id"))
			oOrder.setId(oMapOrder.get("id").toString());

		if (oMapOrder.containsKey("price"))
			oOrder.setPrice(MathUtils.fromString(oMapOrder.get("price").toString()));
		
		if (oMapOrder.containsKey("state"))
			oOrder.setState(oMapOrder.get("state").toString());
		
		if (oMapOrder.containsKey("remaining_volume"))
			oOrder.setVolume(MathUtils.fromString(oMapOrder.get("remaining_volume").toString()));
		else
			oOrder.setVolume(MathUtils.fromString(oMapOrder.get("volume").toString()));
		
		if (oMapOrder.containsKey("created_at"))
			oOrder.setCreated(oMapOrder.get("created_at").toString().replace("T", " ").replace("Z", ""), "yyyy-MM-hh HH:mm:ss");
		
		return oOrder;
	}
	
	@Override public void restart()
	{
		m_nTimeDelta = null;
	}
	
	@Override public StockUserInfo getUserInfo(final RateInfo oRateInfo) throws Exception
	{
		final StockUserInfo oUserInfo = super.getUserInfo(oRateInfo);
		setUserMoney(oUserInfo);
		setUserOrders(oUserInfo, oRateInfo);
		return oUserInfo;
	}
	
	@SuppressWarnings("unchecked")
	public void setUserMoney(final StockUserInfo oUserInfo) throws Exception
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
	
	public void setUserOrders(final StockUserInfo oUserInfo, final RateInfo oRequestRateInfo) throws Exception
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
	
	@Override public Order addOrder(final String strSite, final RateInfo oRateInfo, final BigDecimal nVolume, final BigDecimal nPrice) throws Exception
	{
		super.addOrder(strSite, oRateInfo, nVolume, nPrice);
		
		final Map<String, String> aParameters = new HashMap<String, String>();
		aParameters.put("side", strSite);
		aParameters.put("volume", nVolume.toString());
		aParameters.put("market", getRateIdentifier(oRateInfo));
		aParameters.put("price", nPrice.toString());
		final String strAddOrder = m_strAddOrderUrl.replace("#side#", strSite).replace("#volume#", nVolume.toString())
													.replace("#market#", getRateIdentifier(oRateInfo)).replace("#price#", nPrice.toString());
		final Map<String, Object> oOrder = RequestUtils.sendPostAndReturnJson(signatureUrl(strAddOrder, "POST"), aParameters, true);
		return convert2Order(oOrder);
	}
	
	@Override public Order removeOrder(final String strOrderId) throws Exception
	{
		super.removeOrder(strOrderId);
		
		final Map<String, String> aParameters = new HashMap<String, String>();
		aParameters.put("id", strOrderId);
		final Map<String, Object> oOrder = RequestUtils.sendPostAndReturnJson(signatureUrl(m_strRemoveOrderUrl.replace("#id#", strOrderId), "POST"), aParameters, true);
		return convert2Order(oOrder);
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

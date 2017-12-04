package solo.model.stocks.source;

import java.util.List;
import java.util.Map;

import solo.model.currency.Currency;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.utils.MathUtils;
import solo.utils.RequestUtils;
import ua.lz.ep.utils.ResourceUtils;

public class BtcTradeStockSource extends BaseStockSource
{
	final protected String m_strBuyUrl;
	final protected String m_strSellUrl;
	final protected String m_strDealsUrl;
	
	public BtcTradeStockSource(final IStockExchange oStockExchange)
	{
		super(oStockExchange);
		m_strBuyUrl = ResourceUtils.getResource("buy.url", getStockExchange().getStockProperties());
		m_strSellUrl = ResourceUtils.getResource("sell.url", getStockExchange().getStockProperties());
		m_strDealsUrl = ResourceUtils.getResource("deals.url", getStockExchange().getStockProperties());
		registerRate(new RateInfo(Currency.BTC, Currency.UAH));
		registerRate(new RateInfo(Currency.ETH, Currency.UAH));
	}
	
	@SuppressWarnings("unchecked")
	protected RateState getRateState(final RateInfo oRateInfo) throws Exception
	{
		final RateState oRateState = super.getRateState(oRateInfo);
		
		final String strOrderBuyUrl = m_strBuyUrl.replace("#rate#", getRateIdentifier(oRateInfo));
		final Map<String, Object> oBuyOrders = RequestUtils.sendGetAndReturnMap(strOrderBuyUrl, true);
		final List<Order> oAsksOrders = convert2Orders((List<Object>) oBuyOrders.get("list"));
		oRateState.setAsksOrders(oAsksOrders);
		
		final String strOrderSellUrl = m_strSellUrl.replace("#rate#", getRateIdentifier(oRateInfo));
		final Map<String, Object> oSellOrders = RequestUtils.sendGetAndReturnMap(strOrderSellUrl, true);
		final List<Order> oBidsOrders = convert2Orders((List<Object>) oSellOrders.get("list"));
		oRateState.setBidsOrders(oBidsOrders);
		
		final String strDealsUrl = m_strDealsUrl.replace("#rate#", getRateIdentifier(oRateInfo));
		final List<Object> oInputTrades = RequestUtils.sendGetAndReturnList(strDealsUrl, true);
		final List<Order> oTrades = convert2Orders(oInputTrades);
		oRateState.setTrades(oTrades);
		
		return oRateState;
	}

	protected String getRateIdentifier(final RateInfo oRateInfo)
	{
		return oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + oRateInfo.getCurrencyTo().toString().toLowerCase();  
	}
	
	@Override protected boolean isIgnoreOrder(final Order oOrder)
	{
		return (super.isIgnoreOrder(oOrder) || "ignore".equalsIgnoreCase(oOrder.getState()));
	}
	
	@SuppressWarnings("unchecked")
	@Override protected Order convert2Order(final Object oInputOrder)
	{
		final Map<String, Object> oMapOrder = (Map<String, Object>)oInputOrder;  
		final Order oOrder = new Order();
		if (oMapOrder.containsKey("id"))
			oOrder.setId(oMapOrder.get("id").toString());

		if (oMapOrder.containsKey("price"))
			oOrder.setPrice(MathUtils.fromString(oMapOrder.get("price").toString()));
		
		if (oMapOrder.containsKey("currency_trade"))
			oOrder.setVolume(MathUtils.fromString(oMapOrder.get("currency_trade").toString()));
		
		if (oMapOrder.containsKey("amnt_trade"))
			oOrder.setVolume(MathUtils.fromString(oMapOrder.get("amnt_trade").toString()));

		if (oMapOrder.containsKey("type"))
			oOrder.setState(oMapOrder.get("type").toString().equalsIgnoreCase("sell") ? "ignore" : "buy");
		
		return oOrder;
	}
}

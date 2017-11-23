package solo.model.stocks;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import solo.model.currency.Currency;
import solo.utils.RequestUtils;
import ua.lz.ep.utils.ResourceUtils;

public class KunaStockSource extends BaseStockSource
{
	final protected String m_strOrdersUrl;
	final protected String m_strTradesUrl;
	
	public KunaStockSource(final IStockExchange oStockExchange)
	{
		super(oStockExchange);
		m_strOrdersUrl = ResourceUtils.getResource("orders.url", getStockExchange().getStockProperties());
		m_strTradesUrl = ResourceUtils.getResource("trades.url", getStockExchange().getStockProperties());
		registerRate(new RateInfo(Currency.BTC, Currency.UAH));
		registerRate(new RateInfo(Currency.ETH, Currency.UAH));
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
		final Map<String, Object> oMapOrder = (Map<String, Object>)oInputOrder;  
		final Order oOrder = new Order();
		oOrder.setId(oMapOrder.get("id").toString());

		if (oMapOrder.containsKey("price"))
			oOrder.setPrice(BigDecimal.valueOf(Double.valueOf(oMapOrder.get("price").toString())));
		
		if (oMapOrder.containsKey("state"))
			oOrder.setState(oMapOrder.get("state").toString());
		
		if (oMapOrder.containsKey("remaining_volume"))
			oOrder.setVolume(BigDecimal.valueOf(Double.valueOf(oMapOrder.get("remaining_volume").toString())));
		else
			oOrder.setVolume(BigDecimal.valueOf(Double.valueOf(oMapOrder.get("volume").toString())));
		
		if (oMapOrder.containsKey("created_at"))
			oOrder.setCreated(oMapOrder.get("created_at").toString().replace("T", " ").replace("Z", ""), "yyyy-MM-hh HH:mm:ss");
		
		return oOrder;
	}
}

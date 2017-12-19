package solo.model.stocks.source;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.item.StockUserInfo;
import solo.utils.MathUtils;
import ua.lz.ep.utils.ResourceUtils;

public class BaseStockSource implements IStockSource
{
	final private List<RateInfo> m_aRates = new LinkedList<RateInfo>();
	final protected BigDecimal m_nSumIgnore;
	final protected IStockExchange m_oStockExchange;

	final protected String m_strMoneyUrl;
	final protected String m_strMyOrdersUrl;
	final protected String m_strRemoveOrderUrl;
	final protected String m_strAddOrderUrl;
	final protected String m_strTimeUrl;
	final protected String m_strPublicKey;
	final protected String m_strSecretKey;
	
	public BaseStockSource(final IStockExchange oStockExchange)
	{
		m_oStockExchange = oStockExchange;
		m_nSumIgnore = new BigDecimal(ResourceUtils.getIntFromResource("sum.ignore", getStockExchange().getStockProperties(), 1));
		
		m_strMoneyUrl = ResourceUtils.getResource("money.url", getStockExchange().getStockProperties());
		m_strTimeUrl = ResourceUtils.getResource("time.url", getStockExchange().getStockProperties());
		m_strMyOrdersUrl = ResourceUtils.getResource("my_orders.url", getStockExchange().getStockProperties());
		m_strRemoveOrderUrl = ResourceUtils.getResource("remove_order.url", getStockExchange().getStockProperties());
		m_strAddOrderUrl = ResourceUtils.getResource("add_order.url", getStockExchange().getStockProperties());
		
		m_strPublicKey = ResourceUtils.getResource("trade.public.key", getStockExchange().getStockProperties());
		m_strSecretKey = ResourceUtils.getResource("trade.secret.key", getStockExchange().getStockProperties());
	}
	
	public IStockExchange getStockExchange()
	{
		return m_oStockExchange;
	}
	
	public StockRateStates getStockRates() throws Exception
	{
		final StockRateStates oStockRateStates = new StockRateStates();
		for(final RateInfo oRateInfo : m_aRates)
		{
			final RateState oRateState = getRateState(oRateInfo);
			oStockRateStates.addRate(oRateState);
		}
		return oStockRateStates;
	}
	
	protected RateState getRateState(RateInfo oRateInfo) throws Exception
	{
		return new RateState(oRateInfo);
	}
	
	@Override public StockUserInfo getUserInfo(final RateInfo oRateInfo)
	{
		return new StockUserInfo();
	}
	
	@Override public void restart()
	{
	}

	@Override public Order addOrder(final OrderSide oSide, final RateInfo oRateInfo, final BigDecimal nVolume, final BigDecimal nPrice)
	{
		
		return new Order("cancel", "Order is absent");
	}

	public void checkOrderParameters(final OrderSide oSide, final RateInfo oRateInfo, final BigDecimal nPrice) throws Exception
	{
		final StateAnalysisResult oAnalysisResult = m_oStockExchange.getHistory().getLastAnalysisResult();
		final RateAnalysisResult oRateAnalysisResult = oAnalysisResult.getRateAnalysisResult(oRateInfo);
		final BigDecimal nMinPrice = oRateAnalysisResult.getBidsAnalysisResult().getBestPrice();
		final BigDecimal nMaxPrice = oRateAnalysisResult.getAsksAnalysisResult().getBestPrice();
		if (oSide.equals(OrderSide.SELL) && nPrice.compareTo(nMinPrice) < 0)
			throw new Exception("Because price " + MathUtils.toCurrencyString(nPrice) + " is too small. Current [" + MathUtils.toCurrencyString(nMinPrice) + "]");

		if (oSide.equals(OrderSide.BUY) && nPrice.compareTo(nMaxPrice) > 0)
			throw new Exception("Because price " + MathUtils.toCurrencyString(nPrice) + " is too big. Current [" + MathUtils.toCurrencyString(nMaxPrice) + "]");
	}

	@Override public Order getOrder(String strOrderId, final RateInfo oRateInfo)
	{
		return null;
	}

	@Override public Order removeOrder(String strOrderId)
	{
		return null;
	}

	@Override public List<Order> getTrades(RateInfo m_oRateInfo, final int nPage, final int nCount)
	{
		return null;
	}

	public void registerRate(final RateInfo oRateInfo)
	{
		m_aRates.add(oRateInfo);
	}

	public List<RateInfo> getRates()
	{
		return m_aRates;
	}
	
	protected List<Order> convert2Orders(final List<Object> oInputOrders)
	{
		final List<Order> oOrders = new LinkedList<Order>();
		for(final Object oInputOrder : oInputOrders)
		{
			final Order oOrder = convert2Order(oInputOrder);
			oOrders.add(oOrder);
		}
		return oOrders;
	}

	protected Order convert2Order(final Object oInputOrder)
	{
		final Order oOrder = new Order();
		return oOrder;
	}
}

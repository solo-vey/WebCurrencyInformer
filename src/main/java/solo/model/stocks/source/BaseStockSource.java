package solo.model.stocks.source;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
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
	
	public BaseStockSource(final IStockExchange oStockExchange)
	{
		m_oStockExchange = oStockExchange;
		m_nSumIgnore = new BigDecimal(ResourceUtils.getIntFromResource("sum.ignore", getStockExchange().getStockProperties(), 1));
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
	
	@Override public StockUserInfo getUserInfo() throws Exception
	{
		return new StockUserInfo();
	}

	@Override public Order addOrder(final String strSite, final RateInfo oRateInfo, final BigDecimal nVolume, final BigDecimal nPrice) throws Exception
	{
		final StateAnalysisResult oAnalysisResult = m_oStockExchange.getHistory().getLastAnalysisResult();
		final RateAnalysisResult oRateAnalysisResult = oAnalysisResult.getRateAnalysisResult(oRateInfo);
		final BigDecimal nMinPrice = oRateAnalysisResult.getBidsAnalysisResult().getBestPrice();
		final BigDecimal nMaxPrice = oRateAnalysisResult.getAsksAnalysisResult().getBestPrice();
		if (strSite.equalsIgnoreCase("sell") && nPrice.compareTo(nMinPrice) < 0)
			throw new Exception("Cannot create order. Price " + MathUtils.toCurrencyString(nPrice) + " is too small. Current [" + MathUtils.toCurrencyString(nMinPrice) + "]");

		if (strSite.equalsIgnoreCase("buy") && nPrice.compareTo(nMaxPrice) > 0)
			throw new Exception("Cannot create order. Price " + MathUtils.toCurrencyString(nPrice) + " is too big. Current [" + MathUtils.toCurrencyString(nMaxPrice) + "]");
		
		return null;
	}

	@Override public Order removeOrder(String strOrderId) throws Exception
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
			if (!isIgnoreOrder(oOrder))
				oOrders.add(oOrder);
		}
		return oOrders;
	}

	protected boolean isIgnoreOrder(final Order oOrder)
	{
		return (oOrder.getSum().compareTo(m_nSumIgnore) == -1);
	}

	protected Order convert2Order(final Object oInputOrder)
	{
		final Order oOrder = new Order();
		return oOrder;
	}
}

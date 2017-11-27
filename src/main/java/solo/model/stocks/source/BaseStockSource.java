package solo.model.stocks.source;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.StockRateStates;
import ua.lz.ep.utils.ResourceUtils;

public class BaseStockSource implements IStockSource
{
	private List<RateInfo> m_aRates = new LinkedList<RateInfo>();
	protected BigDecimal m_nSumIgnore;
	protected IStockExchange m_oStockExchange;
	
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

	private boolean isIgnoreOrder(Order oOrder)
	{
		return (oOrder.getSum().compareTo(m_nSumIgnore) == -1);
	}

	protected Order convert2Order(final Object oInputOrder)
	{
		final Order oOrder = new Order();
		return oOrder;
	}
}

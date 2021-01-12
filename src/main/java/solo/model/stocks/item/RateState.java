package solo.model.stocks.item;

import java.util.LinkedList;
import java.util.List;

import solo.model.stocks.BaseObject;

public class RateState extends BaseObject
{
	protected final RateInfo m_oRateInfo;
	protected final List<Order> m_oAsksOrders = new LinkedList<Order>();
	protected final List<Order> m_oBidsOrders = new LinkedList<Order>();
	protected final List<Order> m_oTrades = new LinkedList<Order>();
	
	public RateState(final RateInfo oRateInfo)
	{
		m_oRateInfo = oRateInfo;
	}

	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}
	
	public List<Order>getAsksOrders()
	{
		return m_oAsksOrders;
	}

	public void setAsksOrders(final List<Order> oAsksOrders)
	{
		m_oAsksOrders.clear();
		m_oAsksOrders.addAll(oAsksOrders);
	}
	
	public List<Order> getBidsOrders()
	{
		return m_oBidsOrders;
	}

	public void setBidsOrders(final List<Order> oBidsOrders)
	{
		m_oBidsOrders.clear();
		m_oBidsOrders.addAll(oBidsOrders);
	}
	
	public List<Order> getTrades()
	{
		return (null != m_oTrades ? m_oTrades : new LinkedList<Order>());
	}

	public void setTrades(final List<Order> oTrades)
	{
		m_oTrades.clear();
		m_oTrades.addAll(oTrades);
	}
}

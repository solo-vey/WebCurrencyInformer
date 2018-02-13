package solo.model.stocks.analyse;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import solo.model.stocks.BaseObject;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.StockRateStates;

public class RateAnalysisResult extends BaseObject
{
	final protected RateInfo m_oRateInfo;
	final protected OrderAnalysisResult m_oAsksAnalysisResult;
	final protected OrderAnalysisResult m_oBidsAnalysisResult;
	final protected OrderAnalysisResult m_oTradesAnalysisResult;

	final protected List<Order> m_oAsksOrders;
	final protected List<Order> m_oBidsOrders;
	final protected List<Order> m_oTrades;
	
	public RateAnalysisResult(final StockRateStates oStockRateStates, final RateInfo oRateInfo, final IStockExchange oStockExchange) throws Exception
	{
		this(oStockRateStates.getRate(oRateInfo), oRateInfo, oStockExchange);
	}
	
	public RateAnalysisResult(final RateState oRateState, final RateInfo oRateInfo, final IStockExchange oStockExchange) throws Exception
	{
		m_oRateInfo = oRateInfo;
		m_oAsksOrders = oRateState.getAsksOrders();
		m_oBidsOrders = oRateState.getBidsOrders();
		m_oTrades = oRateState.getTrades();
    	
    	filterOrders(m_oBidsOrders, m_oAsksOrders.get(0).getPrice().divide(new BigDecimal(2)), m_oAsksOrders.get(0).getPrice());
    	filterOrders(m_oAsksOrders, m_oBidsOrders.get(0).getPrice(), m_oBidsOrders.get(0).getPrice().multiply(new BigDecimal(2)));
    	
    	m_oAsksAnalysisResult = new OrderAnalysisResult(m_oAsksOrders);
    	m_oBidsAnalysisResult = new OrderAnalysisResult(m_oBidsOrders);
    	m_oTradesAnalysisResult = new OrderAnalysisResult(m_oTrades);
	}

	private void filterOrders(final List<Order> oOrders, BigDecimal nMinPrice, BigDecimal nMaxPrice)
	{
		final List<Order> oRemoveOrders = new LinkedList<Order>();
		for(final Order oOrder : oOrders)
		{
			if (oOrder.getPrice().compareTo(nMinPrice) < 0 || oOrder.getPrice().compareTo(nMaxPrice) > 0)
				oRemoveOrders.add(oOrder);
		}
		
		for(final Order oOrder : oRemoveOrders)
			oOrders.remove(oOrder);
	}

	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}

	public OrderAnalysisResult getAsksAnalysisResult()
	{
		return m_oAsksAnalysisResult;
	}

	public OrderAnalysisResult getBidsAnalysisResult()
	{
		return m_oBidsAnalysisResult;
	}

	public OrderAnalysisResult getTradesAnalysisResult()
	{
		return m_oTradesAnalysisResult;
	}


	public List<Order> getAsksOrders()
	{
		return m_oAsksOrders;
	}

	public List<Order> getBidsOrders()
	{
		return m_oBidsOrders;
	}

	public List<Order> getTrades()
	{
		return m_oTrades;
	}
}

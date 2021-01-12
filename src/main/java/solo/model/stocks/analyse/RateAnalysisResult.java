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
import solo.model.stocks.item.rules.task.strategy.StrategyUtils;

public class RateAnalysisResult extends BaseObject
{
	protected final RateInfo m_oRateInfo;

	protected final List<Order> m_oAsksOrders;
	protected final List<Order> m_oBidsOrders;
	protected final List<Order> m_oTrades;
	
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
    	
    	filterOrders(m_oBidsOrders, m_oAsksOrders.get(0).getPrice().divide(BigDecimal.valueOf(2)), m_oAsksOrders.get(0).getPrice());
    	filterOrders(m_oAsksOrders, m_oBidsOrders.get(0).getPrice(), m_oBidsOrders.get(0).getPrice().multiply(BigDecimal.valueOf(2)));
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

	public BigDecimal getBestAskPrice()
	{
		return StrategyUtils.getBestPrice(m_oAsksOrders);
	}

	public BigDecimal getBestBidPrice()
	{
		return StrategyUtils.getBestPrice(m_oBidsOrders);
	}

	public BigDecimal getTopTradePrice()
	{
		return StrategyUtils.getBestPrice(m_oTrades);
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

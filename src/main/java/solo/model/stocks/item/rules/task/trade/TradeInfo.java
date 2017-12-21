package solo.model.stocks.item.rules.task.trade;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.rules.task.strategy.IBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.ISellStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickSellStrategy;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;

public class TradeInfo implements Serializable
{
	private static final long serialVersionUID = -7601846839785166296L;

	protected BigDecimal m_nDelta = BigDecimal.ZERO;
	protected Order m_oOrder = Order.NULL;
	protected BigDecimal m_nTradeVolume = BigDecimal.ZERO;
	protected BigDecimal m_nLastOrderPrice = BigDecimal.ZERO;
	protected BigDecimal m_nLastOrderVolume = BigDecimal.ZERO; 

	protected BigDecimal m_nCriticalPrice;
	protected String m_strHistory = StringUtils.EMPTY;
	
	protected IBuyStrategy m_oBuyStrategy = StrategyFactory.getBuyStrategy(QuickBuyStrategy.NAME);
	protected ISellStrategy m_oSellStrategy = StrategyFactory.getSellStrategy(QuickSellStrategy.NAME);
	protected OrderSide m_oTaskSide = OrderSide.BUY; 
	
	public BigDecimal getDelta()
	{
		return m_nDelta;
	}
	
	public Order getOrder()
	{
		return m_oOrder;
	}
	
	public BigDecimal getTradeVolume()
	{
		return m_nTradeVolume;
	}
	
	public BigDecimal getLastOrderPrice()
	{
		return m_nLastOrderPrice;
	}
	
	public BigDecimal getLastOrderVolume()
	{
		return m_nLastOrderVolume;
	}
	
	public BigDecimal getCriticalPrice()
	{
		return m_nCriticalPrice;
	}
	
	public String getM_strHistory()
	{
		return m_strHistory;
	}
	
	public IBuyStrategy getBuyStrategy()
	{
		return m_oBuyStrategy;
	}
	
	public ISellStrategy getSellStrategy()
	{
		return m_oSellStrategy;
	}
	
	public OrderSide getTaskSide()
	{
		return m_oTaskSide;
	}
	
	public void setDelta(BigDecimal nDelta)
	{
		m_nDelta = nDelta;
	}
	
	public void setOrder(Order oOrder)
	{
		m_oOrder = oOrder;
	}
	
	public void setTradeVolume(BigDecimal nTradeVolume)
	{
		m_nTradeVolume = nTradeVolume;
	}
	
	public void setLastOrderPrice(BigDecimal nLastOrderPrice)
	{
		m_nLastOrderPrice = nLastOrderPrice;
	}
	
	public void setLastOrderVolume(BigDecimal nLastOrderVolume)
	{
		m_nLastOrderVolume = nLastOrderVolume;
	}
	
	public void setCriticalPrice(BigDecimal nCriticalPrice)
	{
		m_nCriticalPrice = nCriticalPrice;
	}
	
	public void setHistory(String strHistory)
	{
		m_strHistory = strHistory;
	}
	
	public void setBuyStrategy(IBuyStrategy oBuyStrategy)
	{
		m_oBuyStrategy = oBuyStrategy;
	}
	
	public void setSellStrategy(ISellStrategy oSellStrategy)
	{
		m_oSellStrategy = oSellStrategy;
	}
	
	public void setTaskSide(OrderSide oTaskSide)
	{
		m_oTaskSide = oTaskSide;
	}
}

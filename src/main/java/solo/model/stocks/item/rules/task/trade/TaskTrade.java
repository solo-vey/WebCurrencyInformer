package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TaskTrade extends TaskTradeBase
{
	private static final long serialVersionUID = -178132223787975169L;

	final static public String TRADE_VOLUME = "#volume#";
	final static public String IS_CYCLE = "#isCycle#";
	
	protected Boolean m_bIsCycle; 

	protected Integer m_nTotalCount = 0;
	protected BigDecimal m_nDelta = BigDecimal.ZERO;
	protected BigDecimal m_nTotalDelta = BigDecimal.ZERO;

	public TaskTrade(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine, CommonUtils.mergeParameters(TRADE_VOLUME, IS_CYCLE));
	}

	@Override public String getType()
	{
		return "TRADE";   
	}
	
	@Override public void starTask()
	{
		m_nTradeVolume = getParameterAsBigDecimal(TRADE_VOLUME);
		m_bIsCycle = getParameter(IS_CYCLE).equalsIgnoreCase("true");
		m_oTaskSide = OrderSide.BUY;
	}
	
	@Override protected Order createBuyOrder(final BigDecimal oBuyPrice)
	{
		final BigDecimal oVolume = calculateOrderVolume(m_nTradeVolume, oBuyPrice);
		final Order oBuyOrder = getStockSource().addOrder(OrderSide.BUY, m_oRateInfo, oVolume, oBuyPrice);
		if (oBuyOrder.isNull() || oBuyOrder.isError())
			return Order.NULL;
		
		m_nLastOrderVolume = oVolume;
		m_nLastOrderPrice = oBuyPrice;
		m_nCriticalPrice = TradeUtils.getRoundedPrice(m_oRateInfo, oBuyPrice.multiply(new BigDecimal(1.1)));
		sendMessage("Create " + oBuyOrder.getInfo() + "/" + MathUtils.toCurrencyString(m_nCriticalPrice));
		addToHistory(oBuyOrder.getSide() + " + " + MathUtils.toCurrencyString(oBuyOrder.getPrice()));
		
		return oBuyOrder;
	}

	@Override protected Order createSellOrder(final BigDecimal oSellPrice)
	{
		final BigDecimal oSellOrderPrice = (oSellPrice.compareTo(m_nCriticalPrice) > 0 ? oSellPrice : m_nCriticalPrice); 
		BigDecimal oSellOrderVolume = TradeUtils.getWithoutCommision(m_nLastOrderVolume); 
		oSellOrderVolume = TradeUtils.getRoundedVolume(m_oRateInfo, oSellOrderVolume); 
		final Order oSellOrder = getStockSource().addOrder(OrderSide.SELL, m_oRateInfo, oSellOrderVolume, oSellOrderPrice);
		if (oSellOrder.isNull() || oSellOrder.isError())
			return Order.NULL;

		m_nLastOrderPrice = oSellOrderPrice;
		m_nLastOrderVolume = oSellOrderVolume;
		sendMessage("Create " + oSellOrder.getInfo() + "/" + MathUtils.toCurrencyString(m_nCriticalPrice));
		addToHistory(oSellOrder.getSide() + " + " + MathUtils.toCurrencyString(oSellOrder.getPrice()) + "/" + MathUtils.toCurrencyString(m_nCriticalPrice));
		return oSellOrder;
	}
	
	@Override protected void taskDone(final Order oOrder)
	{
		if (oOrder.isCanceled() || oOrder.isError())
		{
			if (m_oTaskSide.equals(OrderSide.BUY))
			{
				setOrder(Order.NULL);
				return;
			}
			
			sendMessage("Order " + oOrder.getState());
			addToHistory("Order " + oOrder.getState());
			
			super.taskDone(oOrder);
			startNewCycle();
			return;
		}

		if (m_oTaskSide.equals(OrderSide.SELL))
		{
			m_nTotalCount++;

			final BigDecimal nReceivedSum = TradeUtils.getWithoutCommision(m_nLastOrderVolume.multiply(m_nLastOrderPrice));
			m_nDelta = m_nDelta.add(nReceivedSum);
			m_nTotalDelta = m_nTotalDelta.add(m_nDelta);
		
			final String strMessage = "Trade: " + MathUtils.toCurrencyString(nReceivedSum) + "-" + MathUtils.toCurrencyString(nReceivedSum.add(m_nDelta)) + "=" + 
					MathUtils.toCurrencyString(m_nDelta) + "\r\n " + 
					"All: " + m_nTotalCount + "/" + MathUtils.toCurrencyString(m_nTradeVolume) + "/" + MathUtils.toCurrencyString(m_nTotalDelta);
					
			sendMessage(strMessage);
			addToHistory(strMessage);

			super.taskDone(oOrder);
			startNewCycle();
			return;
		}
		
		m_nDelta = m_nLastOrderPrice.multiply(m_nLastOrderVolume).negate();
		sendMessage(getInfo(null) + " is executed");
		
		final BigDecimal nTradeCommision = TradeUtils.getCommisionValue(m_nLastOrderPrice, m_nLastOrderPrice);
		final BigDecimal nTradeMargin = TradeUtils.getMarginValue(m_nLastOrderPrice);
		m_nCriticalPrice = TradeUtils.getRoundedPrice(m_oRateInfo, m_nLastOrderPrice.add(nTradeCommision).add(nTradeMargin));
		final String strMessage = "Set critical price " + MathUtils.toCurrencyString(m_nCriticalPrice) + "/" + MathUtils.toCurrencyString(nTradeCommision) + "/" + MathUtils.toCurrencyString(nTradeMargin);
		sendMessage(strMessage);
		addToHistory("Set critical price " + strMessage);

		setOrder(Order.NULL);
		m_oTaskSide = OrderSide.SELL;
	}
	
	@Override protected void removeTask()
	{
		if (!m_bIsCycle)
			super.removeTask();
	}

	protected void startNewCycle()
	{
		if (!m_bIsCycle)
			return;
			
		m_nLastOrderVolume = BigDecimal.ZERO; 
		m_oTaskSide = OrderSide.BUY;
		setOrder(Order.NULL);
	}
}


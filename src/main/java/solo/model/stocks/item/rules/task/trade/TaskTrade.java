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

	protected BigDecimal m_nBuyVolume = BigDecimal.ZERO; 
	protected BigDecimal m_nSellVolume = BigDecimal.ZERO; 
	
	protected Integer m_nTotalCount = 0;
	protected BigDecimal m_nSpendSum = BigDecimal.ZERO;
	protected BigDecimal m_nTotalDelta = BigDecimal.ZERO;

	public TaskTrade(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine, CommonUtils.mergeParameters(TRADE_VOLUME, IS_CYCLE));
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
		if (oBuyOrder.isNull())
			return oBuyOrder;
		
		m_nBuyVolume = oVolume;
		m_nLastOrderPrice = oBuyPrice;
		m_nCriticalPrice = TradeUtils.getRoundedPrice(m_oRateInfo, oBuyPrice.multiply(new BigDecimal(1.1)));
		sendMessage(getType() + "/Create " + oBuyOrder.getInfo());
		addToHistory(oBuyOrder.getSide() + " + " + MathUtils.toCurrencyString(oBuyOrder.getPrice()));
		
		return oBuyOrder;
	}

	@Override protected Order createSellOrder(final BigDecimal oSellPrice)
	{
		final BigDecimal nTradeCommision = TradeUtils.getCommisionValue(m_nLastOrderPrice, m_nLastOrderPrice);
		final BigDecimal nTradeMargin = TradeUtils.getMarginValue(m_nLastOrderPrice);
		
		m_nCriticalPrice = TradeUtils.getRoundedPrice(m_oRateInfo, m_nLastOrderPrice.add(nTradeCommision).add(nTradeMargin));
		final BigDecimal oSellOrderPrice = (oSellPrice.compareTo(m_nCriticalPrice) > 0 ? oSellPrice : m_nCriticalPrice); 
		BigDecimal oSellOrderVolume = TradeUtils.getWithoutCommision(m_nBuyVolume); 
		oSellOrderVolume = TradeUtils.getRoundedVolume(m_oRateInfo, m_nSellVolume); 
		final Order oSellOrder = getStockSource().addOrder(OrderSide.SELL, m_oRateInfo, oSellOrderVolume, oSellOrderPrice);
		if (oSellOrder.isNull())
			return oSellOrder;

		m_nLastOrderPrice = oSellOrderPrice;
		m_nSellVolume = oSellOrderVolume;
		sendMessage(getType() + "/Create " + oSellOrder.getInfo() + "/" + MathUtils.toCurrencyString(m_nCriticalPrice) + "/" + MathUtils.toCurrencyString(nTradeCommision) + "/" + MathUtils.toCurrencyString(nTradeMargin));
		addToHistory(oSellOrder.getSide() + " + " + MathUtils.toCurrencyString(oSellOrder.getPrice()) + "/" + MathUtils.toCurrencyString(m_nCriticalPrice) + "/" + MathUtils.toCurrencyString(nTradeCommision) + "/" + MathUtils.toCurrencyString(nTradeMargin));
		return oSellOrder;
	}

	@Override public String getType()
	{
		return "TRADE";   
	}
	
	@Override protected void taskDone()
	{
		if (getOrder().isCanceled())
		{
			sendMessage("Order canceled");
			addToHistory("Order canceled");
			
			super.taskDone();
			startNewCycle();
			return;
		}

		if (m_oTaskSide.equals(OrderSide.SELL))
		{
			m_nTotalCount++;

			final BigDecimal nReceivedSum = TradeUtils.getWithoutCommision(m_nSellVolume.multiply(m_nLastOrderPrice));
			final BigDecimal nDelta = nReceivedSum.add(m_nSpendSum.negate());
			m_nTotalDelta = m_nTotalDelta.add(nDelta);
		
			final String strMessage = "Trade: " + MathUtils.toCurrencyString(nReceivedSum) + "-" + MathUtils.toCurrencyString(m_nSpendSum) + "=" + 
					MathUtils.toCurrencyString(nDelta) + "\r\n " + 
					"All: " + m_nTotalCount + "/" + MathUtils.toCurrencyString(m_nTradeVolume) + "/" + MathUtils.toCurrencyString(m_nTotalDelta);
					
			sendMessage(strMessage);
			addToHistory(strMessage);

			super.taskDone();
			startNewCycle();
			return;
		}
		
		m_nSpendSum = m_nLastOrderPrice.multiply(m_nBuyVolume);
		sendMessage(getInfo(null) + " is executed");

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
			
		m_nBuyVolume = BigDecimal.ZERO; 
		m_nSellVolume = BigDecimal.ZERO; 
		m_oTaskSide = OrderSide.BUY;
		setOrder(Order.NULL);
	}
}


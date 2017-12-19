package solo.model.stocks.item.rules.task;

import java.math.BigDecimal;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;
import ua.lz.ep.utils.ResourceUtils;

public class TaskQuickTrade extends TaskQuickBase
{
	private static final long serialVersionUID = -178132223787975169L;

	final static public String TRADE_VOLUME = "#volume#";
	final static public String IS_CYCLE = "#isCycle#";
	
	protected BigDecimal m_nTradeVolume; 
	protected Boolean m_bIsCycle; 

	protected BigDecimal m_nBuyVolume = BigDecimal.ZERO; 
	protected BigDecimal m_nSellVolume = BigDecimal.ZERO; 
	
	protected Integer m_nTotalCount = 0;
	protected BigDecimal m_nSpendSum = BigDecimal.ZERO;
	protected BigDecimal m_nTotalDelta = BigDecimal.ZERO;

	public TaskQuickTrade(final RateInfo oRateInfo, final String strCommandLine) throws Exception
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
		m_nCriticalPrice = MathUtils.getBigDecimal(oBuyPrice.doubleValue() * 1.1, 0);
		sendMessage(getType() + "/Create " + oBuyOrder.getInfo());
		addToHistory(oBuyOrder.getSide() + " + " + MathUtils.toCurrencyString(oBuyOrder.getPrice()));
		
		return oBuyOrder;
	}

	@Override protected Order createSellOrder(final BigDecimal oSellPrice)
	{
		final BigDecimal nStockCommision = new BigDecimal(ResourceUtils.getIntFromResource("stock.commision", getStockExchange().getStockProperties(), 25));
		final BigDecimal nCommision = nStockCommision.divide(new BigDecimal(10000));
		final BigDecimal nTradeCommision = m_nLastOrderPrice.multiply(nCommision);
		
		final BigDecimal nStockMargin = new BigDecimal(ResourceUtils.getIntFromResource("stock.margin", getStockExchange().getStockProperties(), nStockCommision.intValue()));
		final BigDecimal nMargin = nStockMargin.divide(new BigDecimal(10000));
		final BigDecimal nTradeMargin = m_nLastOrderPrice.multiply(nMargin);
		
		m_nCriticalPrice = m_nLastOrderPrice.add(nTradeCommision).add(nTradeMargin);
		final BigDecimal oSellOrderPrice = (oSellPrice.compareTo(m_nCriticalPrice) > 0 ? oSellPrice : m_nCriticalPrice); 
		final BigDecimal oSellOrderVolume = MathUtils.getBigDecimal(m_nBuyVolume.add(m_nBuyVolume.multiply(nCommision).negate()).doubleValue(), 6); 
		final Order oSellOrder = getStockSource().addOrder(OrderSide.SELL, m_oRateInfo, oSellOrderVolume, oSellOrderPrice);
		if (oSellOrder.isNull())
			return oSellOrder;

		m_nLastOrderPrice = oSellPrice;
		sendMessage(getType() + "/Create " + oSellOrder.getInfo() + "/" + MathUtils.toCurrencyString(m_nCriticalPrice) + "/" + MathUtils.toCurrencyString(nTradeCommision) + "/" + MathUtils.toCurrencyString(nTradeMargin));
		addToHistory(getOrder().getSide() + " + " + MathUtils.toCurrencyString(getOrder().getPrice()) + "/" + MathUtils.toCurrencyString(m_nCriticalPrice) + "/" + MathUtils.toCurrencyString(nTradeCommision) + "/" + MathUtils.toCurrencyString(nTradeMargin));
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

			final BigDecimal nStockCommision = new BigDecimal(ResourceUtils.getIntFromResource("stock.commision", getStockExchange().getStockProperties(), 25));
			final BigDecimal nCommision = nStockCommision.divide(new BigDecimal(10000));
			BigDecimal nReceivedSum = m_nSellVolume.multiply(m_nLastOrderPrice);
			nReceivedSum = nReceivedSum.add(nReceivedSum.multiply(nCommision).negate());
			
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


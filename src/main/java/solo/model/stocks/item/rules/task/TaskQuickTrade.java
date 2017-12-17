package solo.model.stocks.item.rules.task;

import java.math.BigDecimal;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TaskQuickTrade extends TaskQuickBase
{
	private static final long serialVersionUID = -178132223787975169L;

	final static public String TRADE_VOLUME = "#volume#";
	final static public String IS_CYCLE = "#isCycle#";
	
	protected BigDecimal m_nTradeVolume; 
	protected Boolean m_bIsCycle; 
	protected OrderSide m_oTaskSide = OrderSide.BUY; 
	protected BigDecimal m_nSpendSum = BigDecimal.ZERO;
	protected BigDecimal m_nReceivedSum = BigDecimal.ZERO;
	protected Integer m_nTotalCount = 0;
	protected BigDecimal m_nTotalDelta = BigDecimal.ZERO;

	public TaskQuickTrade(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine, CommonUtils.mergeParameters(TRADE_VOLUME, IS_CYCLE));
	}
	
	@Override public void starTask()
	{
		m_nTradeVolume = getParameterAsBigDecimal(TRADE_VOLUME);
		
		final StateAnalysisResult oLastStateAnalysisResult = getStockExchange().getHistory().getLastAnalysisResult();
		final BigDecimal oBidPrice = getBuyPrice(oLastStateAnalysisResult);
		final BigDecimal oVolume = calculateOrderVolume(m_nTradeVolume, oBidPrice);
		m_oOrder = getStockSource().addOrder(OrderSide.BUY, m_oRateInfo, oVolume, oBidPrice);
		m_nCriticalPrice = MathUtils.getBigDecimal(oBidPrice.doubleValue() * 1.1, 0);
		
		sendMessage(getType() + "/Create " + m_oOrder.getInfo());
		m_bIsCycle = getParameter(IS_CYCLE).equalsIgnoreCase("true");
		m_oTaskSide = OrderSide.BUY;
		addToHistory(m_oOrder.getSide() + " + " + MathUtils.toCurrencyString(m_oOrder.getPrice()));
	}

	@Override public String getType()
	{
		return "QUICKTRADE";   
	}
	
	@Override protected void taskDone()
	{
		if ("cancel".equalsIgnoreCase(m_oOrder.getState()) || m_oTaskSide.equals(OrderSide.SELL))
		{
			m_nReceivedSum = m_oOrder.getSum();
			m_nTotalCount++;
			final BigDecimal nDelta = m_nReceivedSum.add(m_nSpendSum.negate());
			m_nTotalDelta = m_nTotalDelta.add(nDelta);
			
			final String strMessage = "Result: " + MathUtils.toCurrencyString(m_nReceivedSum) + "-" + MathUtils.toCurrencyString(m_nSpendSum) + "=" + 
					MathUtils.toCurrencyString(nDelta) + "\r\n " + 
					"Statistic: " + m_nTotalCount + "/" + MathUtils.toCurrencyString(m_nTradeVolume) + "/" + MathUtils.toCurrencyString(m_nTotalDelta);
			sendMessage(strMessage);
			addToHistory(strMessage);

			super.taskDone();
			startNewCycle();
			return;
		}
		
		final Order oBuyOrder = getStockSource().getOrder(m_oOrder.getId(), m_oRateInfo);
		if ("cancel".equalsIgnoreCase(oBuyOrder.getState()))
		{
			m_oOrder.setState("cancel");
			super.taskDone();
			startNewCycle();
			return;
		}
	
		m_nSpendSum = oBuyOrder.getSum();
		sendMessage(getInfo(null) + " is executed");

		final StateAnalysisResult oLastStateAnalysisResult = getStockExchange().getHistory().getLastAnalysisResult();
		final BigDecimal oAskPrice = getSellPrice(oLastStateAnalysisResult);
		m_nCriticalPrice = MathUtils.getBigDecimal(oBuyOrder.getPrice().doubleValue() * 1.007, 0);
		final BigDecimal oSellOrderPrice = (oAskPrice.compareTo(m_nCriticalPrice) > 0 ? oAskPrice : m_nCriticalPrice); 
		final BigDecimal oSellOrderVolume = MathUtils.getBigDecimal(oBuyOrder.getVolume().doubleValue() * 0.9975, 6); 
		final Order oSellOrder = getStockSource().addOrder(OrderSide.SELL, m_oRateInfo, oSellOrderVolume, oSellOrderPrice);

		sendMessage(getType() + "/Create " + oSellOrder.getInfo());
		m_oOrder = oSellOrder;
		m_oTaskSide = OrderSide.SELL;
		addToHistory(m_oOrder.getSide() + " + " + MathUtils.toCurrencyString(m_oOrder.getPrice()));
	}
	
	@Override protected void removeTask()
	{
		if (!m_bIsCycle)
			super.removeTask();
	}

	protected void startNewCycle()
	{
		if (m_bIsCycle)
			starTask();
	}
}


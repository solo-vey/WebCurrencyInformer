package solo.model.stocks.item.rules.task;

import java.math.BigDecimal;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
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

	public TaskQuickTrade(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine, TRADE_VOLUME);
	}
	
	@Override public void starTask()
	{
		m_nTradeVolume = getParameterAsBigDecimal(TRADE_VOLUME);
		
		final StateAnalysisResult oLastStateAnalysisResult = getStockExchange().getHistory().getLastAnalysisResult();
		final BigDecimal oBidPrice = oLastStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getBidsAnalysisResult().getBestPrice();
		final BigDecimal oVolume = calculateOrderVolume(m_nTradeVolume, oBidPrice);
		m_oOrder = getStockSource().addOrder(OrderSide.BUY, m_oRateInfo, oVolume, oBidPrice);
		m_nCriticalPrice = MathUtils.getBigDecimal(oBidPrice.doubleValue() * 1.1, 5);
		sendMessage(getType() + "/Create " + m_oOrder.getInfo());
		m_bIsCycle = getParameter(IS_CYCLE).equalsIgnoreCase("true");
		m_oTaskSide = OrderSide.BUY;
	}

	@Override public String getType()
	{
		return "QUICKTRADE";   
	}
	
	@Override protected void taskDone()
	{
		if ("cancel".equalsIgnoreCase(m_oOrder.getState()) || m_oTaskSide.equals(OrderSide.SELL))
		{
			if ("cancel".equalsIgnoreCase(m_oOrder.getState()))
				m_nReceivedSum = m_oOrder.getSum();
			
			sendMessage(getType() + "/Result : " + MathUtils.toCurrencyString(m_nReceivedSum) + " - " + MathUtils.toCurrencyString(m_nSpendSum) + " = " + 
									MathUtils.toCurrencyString(m_nReceivedSum.add(m_nSpendSum.negate())));
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
		final BigDecimal oAskPrice = oLastStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getAsksAnalysisResult().getBestPrice();
		m_nCriticalPrice = MathUtils.getBigDecimal(oBuyOrder.getPrice().doubleValue() * 1.01, 5);
		final BigDecimal oSellOrderPrice = (oAskPrice.compareTo(m_nCriticalPrice) > 0 ? oAskPrice : m_nCriticalPrice); 
		final Order oSellOrder = getStockSource().addOrder(OrderSide.SELL, m_oRateInfo, oBuyOrder.getVolume(), oSellOrderPrice);
		sendMessage(getType() + "/Create " + oSellOrder.getInfo());
		m_oOrder = oSellOrder;
		m_oTaskSide = OrderSide.SELL;
	}

	protected void startNewCycle()
	{
		if (m_bIsCycle)
			starTask();
	}
}


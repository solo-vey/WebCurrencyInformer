package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TaskTradeOld /*extends TaskTrade*/
{
/*	private static final long serialVersionUID = -178132223787975169L;

	final static public String TRADE_VOLUME = "#volume#";
	final static public String IS_CYCLE = "#isCycle#";
	
	protected Boolean m_bIsCycle; 

	public TaskTradeOld(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine, CommonUtils.mergeParameters(TRADE_VOLUME, IS_CYCLE));
	}

	@Override public String getType()
	{
		return "TRADEOLD";   
	}
	
	@Override public void starTask()
	{
		m_oTradeInfo.setTradeSum(getParameterAsBigDecimal(TRADE_VOLUME));
		m_bIsCycle = getParameter(IS_CYCLE).equalsIgnoreCase("true");
		m_oTradeInfo.setTaskSide(OrderSide.BUY);
	}
	
	@Override protected Order createBuyOrder(final BigDecimal oBuyPrice)
	{
		final BigDecimal oBuyOrderPrice = m_oTradeInfo.trimBuyPrice(oBuyPrice); 
		final BigDecimal oVolume = calculateOrderVolume(m_oTradeInfo.getLastOrderSum(), oBuyOrderPrice);
		final Order oBuyOrder = getStockSource().addOrder(OrderSide.BUY, m_oRateInfo, oVolume, oBuyOrderPrice);
		if (oBuyOrder.isNull() || oBuyOrder.isError())
			return Order.NULL;
		
		m_oTradeInfo.setLastOrderVolume(oVolume);
		m_oTradeInfo.setLastOrderPrice(oBuyOrderPrice);
		m_oTradeInfo.setCriticalPrice(oBuyOrderPrice.multiply(new BigDecimal(1.1)));
		sendMessage("Create " + oBuyOrder.getInfo() + "/" + m_oTradeInfo.getCriticalPriceString());
		m_oTradeInfo.addToHistory(oBuyOrder.getSide() + " + " + MathUtils.toCurrencyString(oBuyOrder.getPrice()));
		
		return oBuyOrder;
	}

	@Override protected Order createSellOrder(final BigDecimal oSellPrice)
	{
		final BigDecimal oSellOrderPrice = m_oTradeInfo.trimSellPrice(oSellPrice); 
		BigDecimal oSellOrderVolume = TradeUtils.getWithoutCommision(m_oTradeInfo.getLastOrderVolume()); 
		oSellOrderVolume = TradeUtils.getRoundedVolume(m_oRateInfo, oSellOrderVolume); 
		final Order oSellOrder = getStockSource().addOrder(OrderSide.SELL, m_oRateInfo, oSellOrderVolume, oSellOrderPrice);
		if (oSellOrder.isNull() || oSellOrder.isError())
			return Order.NULL;

		m_oTradeInfo.setLastOrderPrice(oSellOrderPrice);
		m_oTradeInfo.setLastOrderVolume(oSellOrderVolume);
		sendMessage("Create " + oSellOrder.getInfo() + "/" + m_oTradeInfo.getCriticalPriceString());
		m_oTradeInfo.addToHistory(oSellOrder.getSide() + " + " + MathUtils.toCurrencyString(oSellOrder.getPrice()) + "/" + m_oTradeInfo.getCriticalPriceString());
		return oSellOrder;
	}
	
	@Override protected void taskDone(final Order oOrder)
	{
		if (oOrder.isCanceled() || oOrder.isError())
		{
			if (m_oTradeInfo.getTaskSide().equals(OrderSide.BUY))
			{
				m_oTradeInfo.setOrder(Order.NULL);
				return;
			}
			
			sendMessage("Order " + oOrder.getState());
			m_oTradeInfo.addToHistory("Order " + oOrder.getState());
			
			super.taskDone(oOrder);
			startNewCycle();
			return;
		}

		if (m_oTradeInfo.getTaskSide().equals(OrderSide.SELL))
		{
			m_oTradeInfo.addReceivedSum(TradeUtils.getWithoutCommision(m_oTradeInfo.getLastOrderSum()));
			m_oTradeInfo.done();

			sendMessage(m_oTradeInfo.getInfo());
			m_oTradeInfo.addToHistory(m_oTradeInfo.getInfo());

			super.taskDone(oOrder);
			startNewCycle();
			return;
		}
		
		m_oTradeInfo.addSpendSum(m_oTradeInfo.getLastOrderSum());
		sendMessage(getInfo(null) + " is executed");
		
		final BigDecimal nTradeCommision = TradeUtils.getCommisionValue(m_oTradeInfo.getLastOrderPrice(), m_oTradeInfo.getLastOrderPrice());
		final BigDecimal nTradeMargin = TradeUtils.getMarginValue(m_oTradeInfo.getLastOrderPrice());
		m_oTradeInfo.setCriticalPrice(m_oTradeInfo.getLastOrderPrice().add(nTradeCommision).add(nTradeMargin));
		final String strMessage = "Set critical price " + m_oTradeInfo.getCriticalPriceString() + 
									"/" + MathUtils.toCurrencyString(nTradeCommision) + "/" + MathUtils.toCurrencyString(nTradeMargin);
		sendMessage(strMessage);
		m_oTradeInfo.addToHistory(strMessage);

		m_oTradeInfo.setOrder(Order.NULL);
		m_oTradeInfo.setTaskSide(OrderSide.SELL);
	}
	
	@Override protected void removeTask()
	{
		if (!m_bIsCycle)
			super.removeTask();
	}

	protected void startNewCycle()
	{
		if (m_bIsCycle)
			m_oTradeInfo.start();
	}*/
}


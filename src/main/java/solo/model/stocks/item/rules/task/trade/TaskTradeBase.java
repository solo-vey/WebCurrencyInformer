package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.item.rules.task.TaskFactory;
import solo.model.stocks.item.rules.task.strategy.IBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.ISellStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickSellStrategy;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TaskTradeBase extends TaskBase implements ITradeTask
{
	private static final long serialVersionUID = -178132243757975169L;

	final static public String ORDER_ID_PARAMETER = "#orderId#";
	final static public String CRITICAL_PRICE_PARAMETER = "#price#";

	private Order m_oOrder = Order.NULL;
	protected BigDecimal m_nTradeVolume; 
	protected BigDecimal m_nLastOrderPrice; 

	protected BigDecimal m_nCriticalPrice;
	protected String m_strHistory = StringUtils.EMPTY;
	
	protected IBuyStrategy m_oBuyStrategy = StrategyFactory.getBuyStrategy(QuickBuyStrategy.NAME);
	protected ISellStrategy m_oSellStrategy = StrategyFactory.getSellStrategy(QuickSellStrategy.NAME);
	protected OrderSide m_oTaskSide = OrderSide.BUY; 

	public TaskTradeBase(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		this(oRateInfo, strCommandLine, CommonUtils.mergeParameters(ORDER_ID_PARAMETER, CRITICAL_PRICE_PARAMETER));
	}

	public TaskTradeBase(final RateInfo oRateInfo, final String strCommandLine, final String strTemplate) throws Exception
	{
		super(oRateInfo, strCommandLine, strTemplate);
		starTask();
	}
	
	public void starTask() throws Exception
	{
		final String strOrderID = getParameter(ORDER_ID_PARAMETER);
		m_nCriticalPrice = getParameterAsBigDecimal(CRITICAL_PRICE_PARAMETER);
		setOrder(getStockSource().getOrder(strOrderID, m_oRateInfo));
		m_oTaskSide = getOrder().getSide();
		m_nLastOrderPrice = getOrder().getPrice();
		m_nTradeVolume = getOrder().getSum();
	}
	
	public void sendMessage(final String strMessage)
	{
		super.sendMessage(strMessage);
	}
	
	protected void addToHistory(final String strMessage)
	{
		m_strHistory += strMessage + "\r\n";
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return getType() + "/" + getOrder().getInfo() +  
			(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}

	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		if (checkTaskDone(true))
			return;

		final RateAnalysisResult oRateAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
		if (m_oTaskSide.equals(OrderSide.BUY) && getOrder().isNull())
		{
			BigDecimal oBuyPrice = m_oBuyStrategy.getBuyPrice(oRateAnalysisResult, getMyOrders());
			if (oBuyPrice.equals(BigDecimal.ZERO))
				return;
			
			setOrder(createBuyOrder(oBuyPrice));
			return;
		}

		if (m_oTaskSide.equals(OrderSide.SELL) && getOrder().isNull())
		{
			BigDecimal oSellPrice = m_oSellStrategy.getSellPrice(oRateAnalysisResult, getMyOrders());
			if (oSellPrice.equals(BigDecimal.ZERO))
				return;
			
			setOrder(createSellOrder(oSellPrice));
			return;
		}
		
		if (getOrder().getSide().equals(OrderSide.BUY))
		{
			BigDecimal oBuyPrice = m_oBuyStrategy.getBuyPrice(oRateAnalysisResult, getMyOrders());
			oBuyPrice = (oBuyPrice.compareTo(m_nCriticalPrice) > 0 ? m_nCriticalPrice : oBuyPrice);
			setNewOrderPrice(oBuyPrice, getOrder().getId(), true);
			return;
		}

		if (getOrder().getSide().equals(OrderSide.SELL))
		{
			BigDecimal oSellPrice = m_oSellStrategy.getSellPrice(oRateAnalysisResult, getMyOrders());
			oSellPrice = (oSellPrice.compareTo(m_nCriticalPrice) < 0 ? m_nCriticalPrice : oSellPrice);
			setNewOrderPrice(oSellPrice, getOrder().getId(), false);
			return;
		}
	}

	protected Order createBuyOrder(BigDecimal oBuyPrice)
	{
		return Order.NULL;
	}

	protected Order createSellOrder(BigDecimal oSellPrice)
	{
		return Order.NULL;
	}
	
	public Order getOrder()
	{
		return m_oOrder;
	}
	
	protected void setOrder(final Order oOrder)
	{
		m_oOrder = oOrder;
	}
	
	protected List<Order> getMyOrders()
	{
		final List<Order> oMyOrders = new LinkedList<Order>();
		for(final IRule oRule : getStockExchange().getRules().getRules().values())
		{
			if (!(oRule instanceof TaskFactory))
				continue;
					
			final TaskBase oTask = ((TaskFactory)oRule).getTaskBase();
			if (!(oTask instanceof ITradeTask))
				continue;
			
			final Order oOrder = ((ITradeTask)oTask).getOrder();
			if (!oOrder.isNull())
				oMyOrders.add(oOrder);
		}
		
		return oMyOrders;
	}
	
	protected void setNewOrderPrice(final BigDecimal oNewPrice, final String strOrderID, final boolean bIsRecalcVolume)
	{
		if (oNewPrice.compareTo(getOrder().getPrice()) == 0)
			return;
		
		String strMessage = getType() + " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, getStockExchange().getRules().getRuleID(this)) + "\r\n"; 
		final String strHistory = getOrder().getSide() + " " + (getOrder().getPrice().compareTo(oNewPrice) < 0 ? "^ " : "v ") + MathUtils.toCurrencyString(oNewPrice);
		final Order oRemoveOrder = getStockSource().removeOrder(getOrder().getId());
		if (oRemoveOrder.equals(Order.NULL))
			return;
		
		strMessage += "- " + getOrder().getInfoShort() + "\r\n"; 
		final BigDecimal oNewVolume = (bIsRecalcVolume ? calculateOrderVolume(m_nTradeVolume, oNewPrice) : getOrder().getVolume());
		setOrder(getStockSource().addOrder(getOrder().getSide(), m_oRateInfo, oNewVolume, oNewPrice));
		strMessage += "+ " + getOrder().getInfo() + "\r\n";
		m_nLastOrderPrice = oNewPrice;
		
		getStockExchange().getRules().save();
		sendMessage(strMessage);
		addToHistory(strHistory);
	}
	
	protected BigDecimal calculateOrderVolume(final BigDecimal nTradeVolume, final BigDecimal nPrice)
	{
		return MathUtils.getBigDecimal(nTradeVolume.doubleValue() / nPrice.doubleValue(), 6);
	}

	protected boolean checkTaskDone(final boolean bIsReloadOrder)
	{
		if (getOrder().isNull())
			return false;
		
		if (bIsReloadOrder)
		{
			final Order oGetOrder = getStockSource().getOrder(getOrder().getId(), m_oRateInfo);
			if (!oGetOrder.isNull())
				setOrder(oGetOrder);
		}
			
		if (getOrder().isCanceled() || getOrder().isDone())
		{
			taskDone();
			return true;
		}
		
		return false;
	}
	
	protected void removeTask()
	{
		getStockExchange().getRules().removeRule(this);
	}

	protected void taskDone()
	{
		removeTask();
		sendMessage(getInfo(null));
		super.sendMessage(m_strHistory);
		setOrder(Order.NULL);
		m_strHistory = StringUtils.EMPTY;
	}
}


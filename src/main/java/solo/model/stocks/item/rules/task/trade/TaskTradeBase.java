package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.item.rules.task.strategy.IBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.ISellStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickSellStrategy;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;
import ua.lz.ep.utils.ResourceUtils;

public class TaskTradeBase extends TaskBase implements ITradeTask
{
	private static final long serialVersionUID = -178132243757975169L;

	final static public String ORDER_ID_PARAMETER = "#orderId#";
	final static public String CRITICAL_PRICE_PARAMETER = "#price#";

	private Order m_oOrder = Order.NULL;
	protected BigDecimal m_nTradeVolume = BigDecimal.ZERO;
	protected BigDecimal m_nLastOrderPrice = BigDecimal.ZERO;
	protected BigDecimal m_nLastOrderVolume = BigDecimal.ZERO; 

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
		String strMessageTitle = getType() + " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, getStockExchange().getRules().getRuleID(this)) + "\r\n"; 
		super.sendMessage(strMessageTitle + strMessage);
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
		checkTaskDone();

		final RateAnalysisResult oRateAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
		if (m_oTaskSide.equals(OrderSide.BUY) && getOrder().isNull())
		{
			BigDecimal oBuyPrice = m_oBuyStrategy.getBuyPrice(oRateAnalysisResult);
			if (oBuyPrice.equals(BigDecimal.ZERO))
				return;
			
			setOrder(createBuyOrder(oBuyPrice));
			return;
		}

		if (m_oTaskSide.equals(OrderSide.SELL) && getOrder().isNull())
		{
			BigDecimal oSellPrice = m_oSellStrategy.getSellPrice(oRateAnalysisResult);
			if (oSellPrice.equals(BigDecimal.ZERO))
				return;
			
			setOrder(createSellOrder(oSellPrice));
			return;
		}
		
		if (getOrder().getSide().equals(OrderSide.BUY))
		{
			BigDecimal oBuyPrice = m_oBuyStrategy.getBuyPrice(oRateAnalysisResult);
			oBuyPrice = (oBuyPrice.compareTo(m_nCriticalPrice) > 0 ? m_nCriticalPrice : oBuyPrice);
			setNewOrderPrice(oBuyPrice, getOrder().getId(), true);
			return;
		}

		if (getOrder().getSide().equals(OrderSide.SELL))
		{
			BigDecimal oSellPrice = m_oSellStrategy.getSellPrice(oRateAnalysisResult);
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
	
	protected void setNewOrderPrice(final BigDecimal oNewPrice, final String strOrderID, final boolean bIsRecalcVolume)
	{
		if (oNewPrice.compareTo(getOrder().getPrice()) == 0)
			return;
		
		final Order oGetOrder = getStockSource().getOrder(getOrder().getId(), m_oRateInfo);
		if (oGetOrder.isCanceled() || oGetOrder.isDone())
			return;

		final String strMarket = m_oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + m_oRateInfo.getCurrencyTo().toString().toLowerCase(); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strMinVolume = ResourceUtils.getResource("stock." + strMarket + ".min_volume", oStockExchange.getStockProperties(), "0.000001");
		final BigDecimal nMinTradeVolume = MathUtils.getBigDecimal(Double.parseDouble(strMinVolume), TradeUtils.getVolumePrecision(m_oRateInfo));
		if (oGetOrder.getVolume().compareTo(nMinTradeVolume) < 0)
			return;

		String strMessage = StringUtils.EMPTY; 
		final Order oRemoveOrder = getStockSource().removeOrder(getOrder().getId());
		if (oRemoveOrder.isNull() || oRemoveOrder.isError())
			sendMessage("Cannot delete order\r\n" + getOrder().getInfoShort() + "\r\n" + oRemoveOrder.getInfoShort());
		
		strMessage += "- " + oGetOrder.getInfoShort() + "\r\n"; 
		final BigDecimal oNewVolume = (bIsRecalcVolume ? calculateOrderVolume(oGetOrder.getSum(), oNewPrice) : oGetOrder.getVolume());
		final Order oAddOrder = getStockSource().addOrder(getOrder().getSide(), m_oRateInfo, oNewVolume, oNewPrice);
		if (oAddOrder.isNull() || oAddOrder.isError())
		{
			sendMessage("Cannot recreate order\r\n" + getOrder().getSide() + "/" + MathUtils.toCurrencyString(oNewVolume) + "/" + MathUtils.toCurrencyString(oNewPrice) + "\r\n" + oAddOrder.getInfoShort());
			setOrder(Order.NULL);
			return;
		}

		setOrder(oAddOrder);
		strMessage += "+ " + getOrder().getInfo() + "\r\n";
		m_nLastOrderPrice = oNewPrice;
		m_nLastOrderVolume = oNewVolume;
		
		getStockExchange().getRules().save();
		sendMessage(strMessage);

		final String strHistory = oGetOrder.getSide() + " " + (oGetOrder.getPrice().compareTo(oNewPrice) < 0 ? "^ " : "v ") + MathUtils.toCurrencyString(oNewPrice) + "/" + MathUtils.toCurrencyString(oNewVolume);
		addToHistory(strHistory);
	}
	
	protected BigDecimal calculateOrderVolume(final BigDecimal nTradeVolume, final BigDecimal nPrice)
	{
		return TradeUtils.getRoundedVolume(m_oRateInfo, new BigDecimal(nTradeVolume.doubleValue() / nPrice.doubleValue()));
	}

	protected void checkTaskDone()
	{
		if (getOrder().isNull())
			return;
		
		Order oGetOrder = getStockSource().getOrder(getOrder().getId(), m_oRateInfo);
		if (oGetOrder.isError())
			return;
		
		if (!oGetOrder.isCanceled() && !oGetOrder.isDone())
			return;

		taskDone(oGetOrder);
	}
	
	protected void removeTask()
	{
		getStockExchange().getRules().removeRule(this);
	}

	protected void taskDone(final Order oOrder)
	{
		removeTask();
		sendMessage("Task done. " + getInfo(null));
		super.sendMessage(m_strHistory);
		setOrder(Order.NULL);
		m_strHistory = StringUtils.EMPTY;
	}
}


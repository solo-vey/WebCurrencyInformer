package solo.model.stocks.item.rules.task;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TaskQuickBase extends TaskBase
{
	private static final long serialVersionUID = -178132243757975169L;

	final static public String ORDER_ID_PARAMETER = "#orderId#";
	final static public String CRITICAL_PRICE_PARAMETER = "#price#";

	protected Order m_oOrder;
	final protected BigDecimal m_nCriticalPrice;

	public TaskQuickBase(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine, CommonUtils.mergeParameters(ORDER_ID_PARAMETER, CRITICAL_PRICE_PARAMETER));
		final String strOrderID = getParameter(ORDER_ID_PARAMETER);
		m_nCriticalPrice = getParameterAsBigDecimal(CRITICAL_PRICE_PARAMETER);
		m_oOrder = getOrder(strOrderID);
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return getType() + "/" + m_oOrder.getId() + "/" + MathUtils.toCurrencyString(m_nCriticalPrice) + "/" + MathUtils.toCurrencyString(m_oOrder.getPrice()) +  
			(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}
	
	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		if (m_oOrder.getSide().equalsIgnoreCase("sell"))
		{
			final BigDecimal oBidPrice = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getBidsAnalysisResult().getBestPrice();
			if (oBidPrice.compareTo(m_oOrder.getPrice()) > 0 && oBidPrice.compareTo(m_nCriticalPrice) < 0)
				setNewOrderPrice(oBidPrice, m_oOrder.getId());
		}
		else if (m_oOrder.getState().equalsIgnoreCase("buy"))
		{
			final BigDecimal oAskPrice = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getAsksAnalysisResult().getBestPrice();
			if (oAskPrice.compareTo(m_oOrder.getPrice()) < 0 && oAskPrice.compareTo(m_nCriticalPrice) > 0)
				setNewOrderPrice(oAskPrice, m_oOrder.getId());
		}
	}
	
	protected Order getOrder(String strOrderID) throws Exception
	{
		if (StringUtils.isBlank(strOrderID))
			throw new Exception("Order id is empty");
		
		final StockUserInfo oUserInfo = getStockSource().getUserInfo(m_oRateInfo);
		final List<Order> aOrders = oUserInfo.getOrders().get(m_oRateInfo);
		if (null != aOrders)
		{
			for(final Order oOrder : aOrders)
			{
				if (oOrder.getId().equalsIgnoreCase(strOrderID))
					return oOrder;
			}
		}
		
		throw new Exception("Order id [" + strOrderID + "] is absent");
	}

	protected void setNewOrderPrice(final BigDecimal oNewPrice, final String strOrderID)
	{
		try
		{
			m_oOrder = getStockSource().removeOrder(m_oOrder.getId());
			if (checkTaskDone(false))
				return;

			m_oOrder = getStockSource().addOrder(m_oOrder.getState(), m_oRateInfo, m_oOrder.getVolume(), oNewPrice);
			getStockExchange().getRules().save();

			sendMessage(getType() + "/Set new order price " + MathUtils.toCurrencyString(m_oOrder.getPrice()) + "/" + 
					MathUtils.toCurrencyString(m_nCriticalPrice) + " [" + m_oOrder.getId() + "] " + 
					CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, getStockExchange().getRules().getRuleID(this)));
		}
		catch(final Exception e) {}
	}

	protected boolean checkTaskDone(final boolean bIsReloadOrder) throws Exception
	{
		if (null != m_oOrder && bIsReloadOrder)
			m_oOrder = getOrder(m_oOrder.getId());
			
		if (null == m_oOrder || "cancel".equalsIgnoreCase(m_oOrder.getState()) || "done".equalsIgnoreCase(m_oOrder.getState()))
		{
			taskDone();
			return true;
		}
		
		return false;
	}

	protected void taskDone()
	{
		if (null == m_oOrder)
			return;
		
		getStockExchange().getRules().removeRule(this);
		sendMessage(getInfo(null) + " removed because order [" + m_oOrder.getId() + "] was " + m_oOrder.getState());
		m_oOrder = null;
	}
}


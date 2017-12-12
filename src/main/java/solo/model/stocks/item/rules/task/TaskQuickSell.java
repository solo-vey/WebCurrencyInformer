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

public class TaskQuickSell extends TaskBase
{
	private static final long serialVersionUID = -178132223657975169L;

	final static public String ORDER_ID_PARAMETER = "#orderId#";
	final static public String MIN_PRICE_PARAMETER = "#minPrice#";

	private String m_strOrderID;
	private BigDecimal m_nOrderPrice;
	final private BigDecimal m_nMinPrice;

	public TaskQuickSell(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine, CommonUtils.mergeParameters(ORDER_ID_PARAMETER, MIN_PRICE_PARAMETER));
		m_strOrderID = getParameter(ORDER_ID_PARAMETER);
		m_nMinPrice = getParameterAsBigDecimal(MIN_PRICE_PARAMETER);
		m_nOrderPrice = getOrderPrice(m_strOrderID);
	}

	@Override public String getType()
	{
		return "QUICKSELL";   
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return getType() + "/" + m_strOrderID + "/" + MathUtils.toCurrencyString(m_nMinPrice) + "/" + MathUtils.toCurrencyString(m_nOrderPrice) +  
			(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}
	
	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		final BigDecimal oAskPrice = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getAsksAnalysisResult().getBestPrice();
		if (oAskPrice.compareTo(m_nOrderPrice) < 0 && oAskPrice.compareTo(m_nMinPrice) > 0)
			setNewOrderPrice(oAskPrice, m_strOrderID);
	}

	private BigDecimal getOrderPrice(String strOrderID) throws Exception
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
					return oOrder.getPrice();
			}
		}
		
		throw new Exception("Order id [" + strOrderID + "] is absent");
	}

	private void setNewOrderPrice(final BigDecimal oNewPrice, final String strOrderID)
	{
		try
		{
			final Order oRemoveOrder = getStockSource().removeOrder(m_strOrderID);
			if (StringUtils.isBlank(oRemoveOrder.getId()) || "cancel".equalsIgnoreCase(oRemoveOrder.getState()) || "done".equalsIgnoreCase(oRemoveOrder.getState()))
			{
				getStockExchange().getRules().removeRule(this);
				sendMessage(getInfo(null) + " removed because order [" + m_strOrderID + "] was " + oRemoveOrder.getState());
				return;
			}

			final Order oAddOrder = getStockSource().addOrder("sell", m_oRateInfo, oRemoveOrder.getVolume(), oNewPrice);
			m_nOrderPrice = oNewPrice;
			m_strOrderID = oAddOrder.getId();
			getStockExchange().getRules().save();
			sendMessage(getType() + "/Set new order price " + MathUtils.toCurrencyString(m_nOrderPrice) + "/" + 
						MathUtils.toCurrencyString(m_nMinPrice) + " [" + m_strOrderID + "] " + 
						CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, getStockExchange().getRules().getRuleID(this)));
		}
		catch(final Exception e) {}
	}
}


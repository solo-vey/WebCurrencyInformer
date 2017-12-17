package solo.model.stocks.item.rules.task;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
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
	protected BigDecimal m_nCriticalPrice;
	protected String m_strHistory = StringUtils.EMPTY;

	public TaskQuickBase(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		this(oRateInfo, strCommandLine, CommonUtils.mergeParameters(ORDER_ID_PARAMETER, CRITICAL_PRICE_PARAMETER));
	}

	public TaskQuickBase(final RateInfo oRateInfo, final String strCommandLine, final String strTemplate) throws Exception
	{
		super(oRateInfo, strCommandLine, strTemplate);
		starTask();
	}
	
	public void starTask() throws Exception
	{
		final String strOrderID = getParameter(ORDER_ID_PARAMETER);
		m_nCriticalPrice = getParameterAsBigDecimal(CRITICAL_PRICE_PARAMETER);
		m_oOrder = getStockSource().getOrder(strOrderID, m_oRateInfo);
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
		return getType() + "/" + m_oOrder.getInfo() +  
			(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}
	
	public void checkOld(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		if (checkTaskDone(true))
			return;

		BigDecimal oAskPrice = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getAsksAnalysisResult().getBestPrice(); 
		BigDecimal oBidPrice = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getBidsAnalysisResult().getBestPrice(); 
		
		final BigDecimal oAskBestVolume = getBestPriceVolume(oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getAsksOrders());
		final BigDecimal oBidBestVolume = getBestPriceVolume(oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getBidsOrders());
		final BigDecimal oAskSecondPrice = getSecondPrice(oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getAsksOrders());
		final BigDecimal oBidSecondPrice = getSecondPrice(oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getBidsOrders());
		
		if (oAskPrice.compareTo(m_oOrder.getPrice()) == 0 && oAskBestVolume.compareTo(m_oOrder.getVolume()) > 0)
			oAskPrice = oAskPrice.add(BigDecimal.ONE.negate());

		if (oBidPrice.compareTo(m_oOrder.getPrice()) == 0 && oBidBestVolume.compareTo(m_oOrder.getVolume()) > 0)
			oBidPrice = oBidPrice.add(BigDecimal.ONE);

		final BigDecimal oDeltaPrice = oAskPrice.add(oBidPrice.negate());
		final BigDecimal oHalfPercent = new BigDecimal(oAskPrice.doubleValue() * 0.005);
		final boolean bIsDeltaTooSmall = (oDeltaPrice.compareTo(oHalfPercent) < 0);
		final BigDecimal oDeltaAskSecondPrice = oAskSecondPrice.add(oAskPrice.negate());
		final BigDecimal oDeltaBidSecondPrice = oBidPrice.add(oBidSecondPrice.negate());

		if (m_oOrder.getSide().equals(OrderSide.SELL))
		{
			if (oAskPrice.compareTo(m_oOrder.getPrice()) < 0 && oAskPrice.compareTo(m_nCriticalPrice) > 0)
			{
				if (!bIsDeltaTooSmall)
					setNewOrderPrice(oAskPrice, m_oOrder.getId(), false);
				else
					setNewOrderPrice(oBidPrice, m_oOrder.getId(), false);
			}
			else
				if (oAskPrice.compareTo(m_oOrder.getPrice()) == 0 && oAskBestVolume.compareTo(m_oOrder.getVolume()) == 0 && 
						oDeltaAskSecondPrice.compareTo(BigDecimal.ONE) > 0)
					setNewOrderPrice(oAskSecondPrice.add(BigDecimal.ONE), m_oOrder.getId(), false);
		}
		else if (m_oOrder.getSide().equals(OrderSide.BUY))
		{
			final BigDecimal oCheckPrice = (!bIsDeltaTooSmall ? oBidPrice : oBidSecondPrice);
			if (oCheckPrice.compareTo(m_oOrder.getPrice()) != 0 && oCheckPrice.compareTo(m_nCriticalPrice) < 0)
				setNewOrderPrice(oCheckPrice, m_oOrder.getId(), true);
			else
				if (oBidPrice.compareTo(m_oOrder.getPrice()) == 0 && oBidBestVolume.compareTo(m_oOrder.getVolume()) == 0 && 
						oDeltaBidSecondPrice.compareTo(BigDecimal.ONE) > 0)
					setNewOrderPrice(oBidSecondPrice.add(BigDecimal.ONE.negate()), m_oOrder.getId(), true);
		}
	}
	
	protected BigDecimal getSecondPrice(final List<Order> aOrders)
	{
		final BigDecimal oBestPrice = aOrders.get(0).getPrice();
		for(final Order oOrder : aOrders)
		{
			if (oOrder.getPrice().compareTo(oBestPrice) != 0)
				return oOrder.getPrice();
		}
		
		return oBestPrice;
	}

	protected BigDecimal getBestPriceVolume(final List<Order> aOrders)
	{
		final BigDecimal oBestPrice = aOrders.get(0).getPrice();
		BigDecimal oVolume = BigDecimal.ZERO;
		for(final Order oOrder : aOrders)
		{
			if (oOrder.getPrice().compareTo(oBestPrice) == 0)
				oVolume = oVolume.add(oOrder.getVolume());
		}
		
		return oVolume;
	}

	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		if (checkTaskDone(true))
			return;

		if (m_oOrder.getSide().equals(OrderSide.BUY))
		{
			BigDecimal oBuyPrice = getBuyPrice(oStateAnalysisResult);
			oBuyPrice = (oBuyPrice.compareTo(m_nCriticalPrice) > 0 ? m_nCriticalPrice : oBuyPrice);
			setNewOrderPrice(oBuyPrice, m_oOrder.getId(), true);
			return;
		}

		if (m_oOrder.getSide().equals(OrderSide.SELL))
		{
			BigDecimal oSellPrice = getSellPrice(oStateAnalysisResult);
			oSellPrice = (oSellPrice.compareTo(m_nCriticalPrice) < 0 ? m_nCriticalPrice : oSellPrice);
			setNewOrderPrice(oSellPrice, m_oOrder.getId(), false);
			return;
		}
	}
	
	protected BigDecimal getBuyPrice(final StateAnalysisResult oStateAnalysisResult)
	{
		List<Order> oAsks = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getAsksOrders(); 
		List<Order> oBids = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getBidsOrders();
		
		oAsks = removeMyOrders(oAsks, Arrays.asList(m_oOrder)); 
		oBids = removeMyOrders(oBids, Arrays.asList(m_oOrder));
		if (!isDeltaTooSmall(oAsks, oBids))
			return getBestPrice(oBids).add(BigDecimal.ONE);
		
		oAsks = removeFakeOrders(oAsks, new BigDecimal(500)); 
		oBids = removeFakeOrders(oBids, new BigDecimal(500));
		if (!isDeltaTooSmall(oAsks, oBids))
			return getBestPrice(oBids).add(BigDecimal.ONE);
		
		oAsks = removeTooExpenciveOrders(oAsks); 
		oBids = removeTooExpenciveOrders(oBids);
		if (!isDeltaTooSmall(oAsks, oBids))
			return getBestPrice(oBids).add(BigDecimal.ONE);

		while(true)
		{
			oAsks = removeTopOrders(oAsks); 
			oBids = removeTopOrders(oBids);
			if (!isDeltaTooSmall(oAsks, oBids))
				return getBestPrice(oBids).add(BigDecimal.ONE);
		}
	}

	protected BigDecimal getSellPrice(final StateAnalysisResult oStateAnalysisResult)
	{
		List<Order> oAsks = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getAsksOrders();
		oAsks = removeMyOrders(oAsks, Arrays.asList(m_oOrder));
		oAsks = removeFakeOrders(oAsks, new BigDecimal(500));
		oAsks = removeTooExpenciveOrders(oAsks);
		return getBestPrice(oAsks).add(BigDecimal.ONE.negate());
	}

	protected boolean isDeltaTooSmall(List<Order> oAsks, List<Order> oBids)
	{
		final BigDecimal nAskPrice = getBestPrice(oAsks);
		final BigDecimal nBidPrice = getBestPrice(oBids);
		BigDecimal nDelta = nAskPrice.add(nBidPrice.negate());
		if (nDelta.compareTo(BigDecimal.ZERO) < 0)
			nDelta = nDelta.negate();
		
		final BigDecimal nCommision = new BigDecimal(0.25 / 100);
		final BigDecimal nAskCommision = nAskPrice.multiply(nCommision);
		final BigDecimal nBidCommision = nBidPrice.multiply(nCommision);
		final BigDecimal nFullCommision = nAskCommision.add(nBidCommision);
		
		return (nDelta.compareTo(nFullCommision) < 0);
	}

	protected List<Order> removeFakeOrders(List<Order> oOrders, final BigDecimal nMinSum)
	{
		final List<Order> oResult = new LinkedList<Order>();
		for(final Order oOrder : oOrders)
		{
			if (oOrder.getSum().compareTo(nMinSum) > 0)
				oResult.add(oOrder);
		}
		
		return oResult;
	}

	protected List<Order> removeTooExpenciveOrders(List<Order> oOrders)
	{
		final List<Order> oResult = removeTopOrders(oOrders);
		if (isDeltaTooSmall(oOrders, oResult))
			return oOrders;
		
		return oResult;
	}

	protected List<Order> removeTopOrders(List<Order> oOrders)
	{
		final BigDecimal nTopPrice = getBestPrice(oOrders);
		final List<Order> oResult = new LinkedList<Order>();
		for(final Order oOrder : oOrders)
		{
			if (oOrder.getPrice().compareTo(nTopPrice) != 0)
				oResult.add(oOrder);
		}
		
		return oResult;
	}

	protected List<Order> removeMyOrders(List<Order> oOrders, List<Order> oMyOrders)
	{
		final List<Order> oResult = new LinkedList<Order>();
		for(final Order oOrder : oOrders)
		{
			boolean bIsMyOrder = false;
			for(final Order oMyOrder : oMyOrders)
			{
				if (null != oMyOrder && oOrder.getId().equalsIgnoreCase(oMyOrder.getId()))
					bIsMyOrder = true;
			}
			if (!bIsMyOrder)
				oResult.add(oOrder);
		}
		
		return oResult;
	}

	protected BigDecimal getBestPrice(List<Order> oOrders)
	{
		return oOrders.get(0).getPrice();
	}

	protected void setNewOrderPrice(final BigDecimal oNewPrice, final String strOrderID, final boolean bIsRecalcVolume)
	{
		if (oNewPrice.compareTo(m_oOrder.getPrice()) == 0)
			return;
		
		String strMessage = getType() + " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, getStockExchange().getRules().getRuleID(this)) + "\r\n"; 
		final String strHistory = m_oOrder.getSide() + " " + (m_oOrder.getPrice().compareTo(oNewPrice) < 0 ? "^ " : "v ") + MathUtils.toCurrencyString(oNewPrice);
		m_oOrder = getStockSource().removeOrder(m_oOrder.getId());
		strMessage += "Remove " + m_oOrder.getInfoShort() + "\r\n"; 
		final BigDecimal oNewVolume = (bIsRecalcVolume ? calculateOrderVolume(m_oOrder.getSum(), oNewPrice) : m_oOrder.getVolume());
		m_oOrder = getStockSource().addOrder(m_oOrder.getSide(), m_oRateInfo, oNewVolume, oNewPrice);
		strMessage += "Add " + m_oOrder.getInfo() + "\r\n"; 
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
		if (null != m_oOrder && bIsReloadOrder)
			m_oOrder = getStockSource().getOrder(m_oOrder.getId(), m_oRateInfo);
			
		if ("cancel".equalsIgnoreCase(m_oOrder.getState()) || "done".equalsIgnoreCase(m_oOrder.getState()))
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
		m_oOrder = null;
		m_strHistory = StringUtils.EMPTY;
	}
}


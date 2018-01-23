package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.trade.GetTradeInfoCommand;
import solo.model.stocks.item.command.trade.RemoveOrderCommand;
import solo.model.stocks.item.command.trade.SetTaskParameterCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;
import solo.utils.MathUtils;

public class TaskTrade extends TaskBase implements ITradeTask
{
	private static final long serialVersionUID = -178132243757975169L;

	final static public String TRADE_VOLUME = "#volume#";
	final static public String CRITICAL_PRICE_PARAMETER = "criticalPrice";
	
	protected String m_strCurrentState = StringUtils.EMPTY;

	protected TradeInfo m_oTradeInfo;

	public TaskTrade(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		this(oRateInfo, strCommandLine, TRADE_VOLUME);
	}

	public TaskTrade(final RateInfo oRateInfo, final String strCommandLine, final String strTemplate) throws Exception
	{
		super(oRateInfo, strCommandLine, strTemplate);
		m_oTradeInfo = new TradeInfo(oRateInfo, -1);
		m_oTradeInfo.setTradeSum(getParameterAsBigDecimal(TRADE_VOLUME));
		m_oTradeInfo.setTaskSide(OrderSide.BUY);
	}

	@Override public String getType()
	{
		return "TRADE";   
	}
	
	public TradeInfo getTradeInfo()
	{
		return m_oTradeInfo;
	}
	
	public void starTask() throws Exception
	{
		getTradeControler().tradeStart(this);
	}
	
	public void sendMessage(final String strMessage)
	{
		final String strMessageFooter = getType() +
			" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo) +   
			" " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, WorkerFactory.getStockExchange().getRules().getRuleID(this)); 
		WorkerFactory.getMainWorker().sendMessage(strMessage + "\r\n" + strMessageFooter);
	}
	
	public String getInfo(final Integer nRuleID)
	{
		final String strGetRateCommand = (getTradeControler().equals(ITradeControler.NULL) ? 
				CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo) + " " : StringUtils.EMPTY);

		String strQuickSell = StringUtils.EMPTY;
		if (getTradeInfo().getTaskSide().equals(OrderSide.SELL))
		{
			strQuickSell = " " + CommandFactory.makeCommandLine(SetTaskParameterCommand.class, SetTaskParameterCommand.RULE_ID_PARAMETER, nRuleID, 
					SetTaskParameterCommand.NAME_PARAMETER, TaskTrade.CRITICAL_PRICE_PARAMETER, 
					SetTaskParameterCommand.VALUE_PARAMETER, getTradeInfo().getCriticalPriceString()) + 
					" " + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, nRuleID, GetTradeInfoCommand.FULL_PARAMETER, "true") + 
					"\r\n";
		}
		
		if (StringUtils.isNotBlank(m_strCurrentState))
			strQuickSell += "State [" + m_strCurrentState + "]\r\n";
		
		return getType() + "/" + (m_oTradeInfo.getOrder().equals(Order.NULL) ?  m_oTradeInfo.getTaskSide() + "/" : StringUtils.EMPTY) + 
					m_oTradeInfo.getOrder().getInfoShort() + "\r\n" + 
					strGetRateCommand + strQuickSell + 
					CommandFactory.makeCommandLine(RemoveOrderCommand.class, RemoveOrderCommand.ID_PARAMETER, m_oTradeInfo.getOrder().getId()) + 
					(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}

	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		m_strCurrentState = StringUtils.EMPTY;
		final Order oGetOrder = updateTradeInfo(m_oTradeInfo.getOrder());
		checkTaskDone(oGetOrder);

		final RateAnalysisResult oRateAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
		if (m_oTradeInfo.getTaskSide().equals(OrderSide.BUY) && oGetOrder.isNull())
		{
			BigDecimal oBuyPrice = m_oTradeInfo.getBuyStrategy().getBuyPrice(oRateAnalysisResult, getTradeInfo());
			if (oBuyPrice.equals(BigDecimal.ZERO))
				return;
			
			m_oTradeInfo.setOrder(createBuyOrder(oBuyPrice));
			WorkerFactory.getStockExchange().getRules().save();
			return;
		}

		if (m_oTradeInfo.getTaskSide().equals(OrderSide.SELL) && oGetOrder.isNull())
		{
			BigDecimal oSellPrice = m_oTradeInfo.getSellStrategy().getSellPrice(oRateAnalysisResult, getTradeInfo());
			if (oSellPrice.equals(BigDecimal.ZERO))
				return;
			
			m_oTradeInfo.setOrder(createSellOrder(oSellPrice));
			WorkerFactory.getStockExchange().getRules().save();
			return;
		}
		
		if (OrderSide.BUY.equals(oGetOrder.getSide()))
		{
			final BigDecimal oBuyPrice = m_oTradeInfo.getBuyStrategy().getBuyPrice(oRateAnalysisResult, getTradeInfo());
			setNewOrderPrice(oBuyPrice, oGetOrder);
			return;
		}

		if (OrderSide.SELL.equals(oGetOrder.getSide()))
		{
			BigDecimal oSellPrice = m_oTradeInfo.getSellStrategy().getSellPrice(oRateAnalysisResult, getTradeInfo());
			oSellPrice = m_oTradeInfo.trimSellPrice(oSellPrice);
			setNewOrderPrice(oSellPrice, oGetOrder);
			return;
		}
	}

	protected Order updateTradeInfo(final Order oOrder)
	{
		if (oOrder.isNull())
			return oOrder;
		
		final Order oGetOrder = WorkerFactory.getStockSource().getOrder(m_oTradeInfo.getOrder().getId(), m_oRateInfo);
		if (oGetOrder.isNull() || oGetOrder.isError())
		{
			m_strCurrentState = "updateTradeInfo - " + oGetOrder;
			return oOrder;
		}

		if (oGetOrder.isCanceled())
			return oGetOrder;
		
		m_oTradeInfo.setOrder(oGetOrder);
		if (oGetOrder.getSide().equals(OrderSide.BUY))
		{
			if (oGetOrder.isDone())
			{
				final BigDecimal nNeedBuyVolume = m_oTradeInfo.getNeedBoughtVolume();
				final BigDecimal nBuyVolume = TradeUtils.getWithoutCommision(nNeedBuyVolume);
				final BigDecimal nDeltaSpendSum = nNeedBuyVolume.multiply(oOrder.getPrice());
				
				m_oTradeInfo.addBuy(nDeltaSpendSum, nBuyVolume);
				m_oTradeInfo.setNeedBoughtVolume(BigDecimal.ZERO);
			}
			else
			{
				final BigDecimal nDeltaSpendSum = m_oTradeInfo.getNeedSpendSum().add(oGetOrder.getSum().negate());
				if (nDeltaSpendSum.compareTo(BigDecimal.ZERO) == 0)
					return oGetOrder;
	
				final BigDecimal nDeltaBoughtVolume = m_oTradeInfo.getNeedBoughtVolume().add(oGetOrder.getVolume().negate());
				m_oTradeInfo.addBuy(nDeltaSpendSum, TradeUtils.getWithoutCommision(nDeltaBoughtVolume));
				m_oTradeInfo.setNeedBoughtVolume(oGetOrder.getVolume());
			}
			WorkerFactory.getStockExchange().getRules().save();
			return oGetOrder;
		}

		if (oGetOrder.getSide().equals(OrderSide.SELL))
		{
			if (oGetOrder.isDone())
			{
				final BigDecimal nSellVolume = m_oTradeInfo.getNeedSellVolume();
				final BigDecimal nDeltaSellSum = nSellVolume.multiply(oOrder.getPrice());
				m_oTradeInfo.addSell(TradeUtils.getWithoutCommision(nDeltaSellSum), nSellVolume);
			}
			else
			{
				final BigDecimal nDeltaSellVolume = m_oTradeInfo.getNeedSellVolume().add(oGetOrder.getVolume().negate());
				final BigDecimal nDeltaSellSum = nDeltaSellVolume.multiply(oGetOrder.getPrice());
				if (nDeltaSellSum.compareTo(BigDecimal.ZERO) == 0)
					return oGetOrder;
				
				m_oTradeInfo.addSell(TradeUtils.getWithoutCommision(nDeltaSellSum), nDeltaSellVolume);
			}
			WorkerFactory.getStockExchange().getRules().save();
			return oGetOrder;
		}
		
		return oGetOrder;
	}

	protected Order createBuyOrder(BigDecimal oBuyPrice)
	{
		final BigDecimal oVolume = calculateOrderVolume(m_oTradeInfo.getNeedSpendSum(), oBuyPrice);
		final Order oBuyOrder = addOrder(OrderSide.BUY, oVolume, oBuyPrice);
		if (oBuyOrder.isNull())
			return oBuyOrder;
		
		m_oTradeInfo.setNeedBoughtVolume(oBuyOrder.getVolume());
		m_oTradeInfo.setTradeSum(oBuyOrder.getSum());
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, "Create " + oBuyOrder.getInfo());
		m_oTradeInfo.addToHistory(oBuyOrder.getSide() + " + " + MathUtils.toCurrencyStringEx2(oBuyOrder.getPrice()));
		return oBuyOrder;
	}

	protected Order createSellOrder(BigDecimal oSellPrice)
	{
		final BigDecimal oSellOrderPrice = m_oTradeInfo.trimSellPrice(oSellPrice); 
		BigDecimal oSellOrderVolume = m_oTradeInfo.getNeedSellVolume(); 
		oSellOrderVolume = TradeUtils.getRoundedVolume(m_oRateInfo, oSellOrderVolume); 
		Order oSellOrder = addOrder(OrderSide.SELL, oSellOrderVolume, oSellOrderPrice);
		if (oSellOrder.isNull())
			return oSellOrder;

		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, "Create " + oSellOrder.getInfo() + "/" + m_oTradeInfo.getCriticalPriceString());
		m_oTradeInfo.addToHistory(oSellOrder.getSide() + " + " + MathUtils.toCurrencyStringEx2(oSellOrder.getPrice()) + "/" + m_oTradeInfo.getCriticalPriceString());
		return oSellOrder;
	}
	
	protected void setNewOrderPrice(final BigDecimal oNewPrice, final Order oGetOrder)
	{
		final BigDecimal nRoundedNewPrice = MathUtils.getBigDecimal(oNewPrice.doubleValue(), TradeUtils.getPricePrecision(m_oRateInfo));
		final BigDecimal nRoundedOrderPrice = MathUtils.getBigDecimal(oGetOrder.getPrice().doubleValue(), TradeUtils.getPricePrecision(m_oRateInfo));
		final BigDecimal nDeltaOrderPrice = (nRoundedNewPrice.compareTo(nRoundedOrderPrice) > 0 ? nRoundedNewPrice.add(nRoundedOrderPrice.negate()) 
																		: nRoundedOrderPrice.add(nRoundedNewPrice.negate()));
		final BigDecimal nMinChangePrice = TradeUtils.getMinChangePrice();
		if (nDeltaOrderPrice.compareTo(BigDecimal.ZERO) >= 0 && nDeltaOrderPrice.compareTo(nMinChangePrice) < 0)
			return;
		
		if (oGetOrder.isCanceled() || oGetOrder.isDone())
			return;

		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(m_oRateInfo);
		if (oGetOrder.getVolume().compareTo(nMinTradeVolume) < 0)
			return;

		final Date oOrderDateCreate = oGetOrder.getCreated();
		final Order oRemoveOrder = removeOrder(oGetOrder);
		if (oRemoveOrder.isException())
			return;
		
		if (oRemoveOrder.isDone())
		{
			m_oTradeInfo.setOrder(oRemoveOrder);
			WorkerFactory.getStockExchange().getRules().save();
			return;
		}
		
		final BigDecimal oNewVolume = (oGetOrder.getSide().equals(OrderSide.BUY) ? calculateOrderVolume(m_oTradeInfo.getNeedSpendSum(), oNewPrice) : oGetOrder.getVolume());
		final Order oAddOrder = addOrder(oGetOrder.getSide(), oNewVolume, oNewPrice);
		if (null != oOrderDateCreate)
			oAddOrder.setCreated(oOrderDateCreate);
		m_oTradeInfo.setOrder(oAddOrder);
		
		if (oGetOrder.getSide().equals(OrderSide.BUY))
		{
			m_oTradeInfo.setNeedBoughtVolume(oAddOrder.getVolume());
			m_oTradeInfo.setTradeSum(oAddOrder.getSum().add(m_oTradeInfo.getSpendSum()));
		}

		final String strMessage = "- " + oGetOrder.getInfoShort() + "\r\n+ " + oAddOrder.getInfo();
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.TRACE, strMessage);
		WorkerFactory.getStockExchange().getRules().save();
	}

	protected Order removeOrder(final Order oGetOrder)
	{
		int nTryCount = 50;
		final String strMessage = "Cannot delete order\r\n" + oGetOrder.getInfoShort();
		Order oRemoveOrder = new Order(Order.ERROR, strMessage);
		while (nTryCount > 0)
		{
			oRemoveOrder = WorkerFactory.getStockSource().removeOrder(oGetOrder.getId());
			if (oRemoveOrder.isCanceled())
				return oRemoveOrder;

			if (!oRemoveOrder.isException())
			{
				final Order oCheckRemoveOrder = WorkerFactory.getStockSource().getOrder(oGetOrder.getId(), m_oRateInfo);
				if (oCheckRemoveOrder.isDone() || oCheckRemoveOrder.isCanceled())
					return oCheckRemoveOrder;
			}
			
			try { Thread.sleep(100); }
			catch (InterruptedException e) { break; }
			nTryCount--;
		}

		WorkerFactory.getMainWorker().sendMessage(MessageLevel.ERROR, strMessage);
		m_strCurrentState = "removeOrder - " + oRemoveOrder;
		return oRemoveOrder;
	}

	protected Order addOrder(final OrderSide oOrderSide, final BigDecimal oVolume, final BigDecimal oPrice)
	{
		int nTryCount = 50;
		Order oAddOrder = Order.NULL;
		while (nTryCount > 0)
		{
			oAddOrder = WorkerFactory.getStockSource().addOrder(oOrderSide, m_oRateInfo, oVolume, oPrice);
			if (!oAddOrder.isException())
				return oAddOrder;
			
			try { Thread.sleep(100); }
			catch (InterruptedException e) { break; }
			nTryCount--;
		}
		
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.ERROR, "Can't create order\r\n" + oOrderSide + "/" + MathUtils.toCurrencyStringEx2(oVolume) + "/" + MathUtils.toCurrencyStringEx2(oPrice) + "\r\n" + oAddOrder.getInfoShort());
		m_strCurrentState = "addOrder - " + oAddOrder;
		return oAddOrder;
	}
	
	protected BigDecimal calculateOrderVolume(final BigDecimal nTradeVolume, final BigDecimal nPrice)
	{
		return TradeUtils.getRoundedVolume(m_oRateInfo, new BigDecimal(nTradeVolume.doubleValue() / nPrice.doubleValue()));
	}
	
	protected void checkTaskDone(final Order oGetOrder)
	{
		if (oGetOrder.isNull() || oGetOrder.isError())
			return;
		
		if (oGetOrder.isCanceled() || oGetOrder.isDone())
			taskDone(oGetOrder);
	}

	protected void taskDone(final Order oOrder)
	{
		if (oOrder.isCanceled() || oOrder.isError())
		{
			if (m_oTradeInfo.getTaskSide().equals(OrderSide.BUY))
			{
				m_oTradeInfo.setOrder(Order.NULL);
				return;
			}
			
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.ERROR, "Order cancel. " + oOrder.getInfoShort());
			m_oTradeInfo.addToHistory("Order cancel. " + oOrder.getInfoShort());
			
			supertaskDone(oOrder);
			return;
		}

		if (m_oTradeInfo.getTaskSide().equals(OrderSide.SELL))
		{
			final BigDecimal nSellVolume = m_oTradeInfo.getNeedSellVolume();
			final BigDecimal nDeltaSellSum = nSellVolume.multiply(oOrder.getPrice());
			m_oTradeInfo.addSell(TradeUtils.getWithoutCommision(nDeltaSellSum), nSellVolume);
			m_oTradeInfo.addToHistory(m_oTradeInfo.getInfo());

			supertaskDone(oOrder);
			return;
		}
		
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, getInfo(null) + " is executed");
		
		m_oTradeInfo.setCriticalPrice(m_oTradeInfo.calculateCriticalPrice());
		final String strMessage = "Set critical price " + m_oTradeInfo.getCriticalPriceString() + 
									"/" + MathUtils.toCurrencyStringEx2(TradeUtils.getCommisionValue(m_oTradeInfo.getAveragedBoughPrice(), m_oTradeInfo.getAveragedBoughPrice())) + 
									"/" + MathUtils.toCurrencyStringEx2(TradeUtils.getMarginValue(m_oTradeInfo.getAveragedBoughPrice())) + 
									"/" + MathUtils.toCurrencyStringEx2(m_oTradeInfo.getPriviousLossSum());
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, strMessage);
		m_oTradeInfo.addToHistory(strMessage);
		getTradeControler().buyDone(this);

		m_oTradeInfo.setOrder(Order.NULL);
		m_oTradeInfo.setTaskSide(OrderSide.SELL);
		WorkerFactory.getStockExchange().getRules().save();
	}

	protected void supertaskDone(final Order oOrder)
	{
		if (getTradeControler().equals(ITradeControler.NULL))
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.TRADERESULT, "Task done. " + getInfo(null) + "\r\n" + m_oTradeInfo.getHistory());

		getTradeControler().tradeDone(this);
		WorkerFactory.getStockExchange().getRules().removeRule(this);
	}
	
	public void remove()
	{
		setTradeControler(ITradeControler.NULL);
	}
	
	@Override public ITradeControler getTradeControler()
	{
		return m_oTradeInfo.getTradeControler();
	}

	public void setTradeControler(final ITradeControler oTradeControler)
	{
		m_oTradeInfo.setTradeControler(oTradeControler);
	}
	
	@Override public void setParameter(final String strParameterName, final String strValue)
	{
		if (strParameterName.equalsIgnoreCase("criticalPrice"))
			m_oTradeInfo.setCriticalPrice(MathUtils.fromString(strValue));

		if (strParameterName.equalsIgnoreCase("attachOrder") && m_oTradeInfo.getOrder().isNull())
		{
			final Order oGetOrder = WorkerFactory.getStockSource().getOrder(strValue, m_oRateInfo);
			if (!oGetOrder.isNull() && !oGetOrder.isError() && !oGetOrder.isCanceled())
			{
				m_oTradeInfo.setOrder(oGetOrder);
				WorkerFactory.getStockExchange().getRules().save();
			}
		}
				
		super.setParameter(strParameterName, strValue);
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		return  m_oRateInfo + "\r\n" + getTradeInfo().getInfo() + 
			(StringUtils.isNotBlank(m_strCurrentState) ? "\r\nState : [" + m_strCurrentState + "]" : StringUtils.EMPTY);
	}
}


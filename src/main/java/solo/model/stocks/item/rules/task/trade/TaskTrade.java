package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.trade.RemoveOrderCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.transport.MessageLevel;
import solo.utils.MathUtils;

public class TaskTrade extends TaskBase implements ITradeTask
{
	private static final long serialVersionUID = -178132243757975169L;

	final static public String TRADE_VOLUME = "#volume#";
	final static public String CRITICAL_PRICE_PARAMETER = "criticalPrice";

	protected TradeInfo m_oTradeInfo;

	public TaskTrade(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		this(oRateInfo, strCommandLine, TRADE_VOLUME);
	}

	public TaskTrade(final RateInfo oRateInfo, final String strCommandLine, final String strTemplate) throws Exception
	{
		super(oRateInfo, strCommandLine, strTemplate);
		m_oTradeInfo = new TradeInfo(oRateInfo);
		starTask();
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
		m_oTradeInfo.setTradeSum(getParameterAsBigDecimal(TRADE_VOLUME));
		m_oTradeInfo.setTaskSide(OrderSide.BUY);
	}
	
	public void sendMessage(final String strMessage)
	{
		final String strMessageFooter = getType() +
			" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo) +   
			" " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, getStockExchange().getRules().getRuleID(this)); 
		super.sendMessage(strMessage + "\r\n" + strMessageFooter);
	}
	
	public String getInfo(final Integer nRuleID)
	{
		final String strGetRateCommand = (getTradeControler().equals(ITradeControler.NULL) ? 
				CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo) + " " : StringUtils.EMPTY);
		return getType() + "/" + (m_oTradeInfo.getOrder().equals(Order.NULL) ?  m_oTradeInfo.getTaskSide() + "/" : StringUtils.EMPTY) + 
					m_oTradeInfo.getOrder().getInfoShort() + "\r\n" + 
					strGetRateCommand +   
					CommandFactory.makeCommandLine(RemoveOrderCommand.class, RemoveOrderCommand.ID_PARAMETER, m_oTradeInfo.getOrder().getId()) + 
					(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}

	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		final Order oGetOrder = updateTradeInfo(m_oTradeInfo.getOrder());
		checkTaskDone(oGetOrder);

		final RateAnalysisResult oRateAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
		if (m_oTradeInfo.getTaskSide().equals(OrderSide.BUY) && oGetOrder.isNull())
		{
			BigDecimal oBuyPrice = m_oTradeInfo.getBuyStrategy().getBuyPrice(oRateAnalysisResult);
			if (oBuyPrice.equals(BigDecimal.ZERO))
				return;
			
			m_oTradeInfo.setOrder(createBuyOrder(oBuyPrice));
			return;
		}

		if (m_oTradeInfo.getTaskSide().equals(OrderSide.SELL) && oGetOrder.isNull())
		{
			BigDecimal oSellPrice = m_oTradeInfo.getSellStrategy().getSellPrice(oRateAnalysisResult);
			if (oSellPrice.equals(BigDecimal.ZERO))
				return;
			
			m_oTradeInfo.setOrder(createSellOrder(oSellPrice));
			return;
		}
		
		if (oGetOrder.getSide().equals(OrderSide.BUY))
		{
			final BigDecimal oBuyPrice = m_oTradeInfo.getBuyStrategy().getBuyPrice(oRateAnalysisResult);
			setNewOrderPrice(oBuyPrice, oGetOrder);
			return;
		}

		if (oGetOrder.getSide().equals(OrderSide.SELL))
		{
			BigDecimal oSellPrice = m_oTradeInfo.getSellStrategy().getSellPrice(oRateAnalysisResult);
			oSellPrice = m_oTradeInfo.trimSellPrice(oSellPrice);
			setNewOrderPrice(oSellPrice, oGetOrder);
			return;
		}
	}

	protected Order updateTradeInfo(final Order oOrder)
	{
		if (oOrder.isNull())
			return oOrder;
		
		final Order oGetOrder = getStockSource().getOrder(m_oTradeInfo.getOrder().getId(), m_oRateInfo);
		if (oGetOrder.isNull() || oGetOrder.isError())
			return oOrder;

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
				m_oTradeInfo.addSell(TradeUtils.getWithoutCommision(nDeltaSellSum), nDeltaSellVolume);
			}
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
		sendMessage(MessageLevel.DEBUG, "Create " + oBuyOrder.getInfo());
		m_oTradeInfo.addToHistory(oBuyOrder.getSide() + " + " + MathUtils.toCurrencyString(oBuyOrder.getPrice()));
		getStockExchange().getRules().save();
		
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

		sendMessage(MessageLevel.DEBUG, "Create " + oSellOrder.getInfo() + "/" + m_oTradeInfo.getCriticalPriceString());
		m_oTradeInfo.addToHistory(oSellOrder.getSide() + " + " + MathUtils.toCurrencyString(oSellOrder.getPrice()) + "/" + m_oTradeInfo.getCriticalPriceString());
		getStockExchange().getRules().save();
		
		return oSellOrder;
	}
	
	protected void setNewOrderPrice(final BigDecimal oNewPrice, final Order oGetOrder)
	{
		if (oNewPrice.compareTo(oGetOrder.getPrice()) == 0)
			return;
		
		if (oGetOrder.isCanceled() || oGetOrder.isDone())
			return;

		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(m_oRateInfo);
		if (oGetOrder.getVolume().compareTo(nMinTradeVolume) < 0)
			return;

		final Order oRemoveOrder = removeOrder(oGetOrder);
		if (oRemoveOrder.isDone())
		{
			getStockExchange().getRules().save();
			return;
		}
		
		final BigDecimal oNewVolume = (oGetOrder.getSide().equals(OrderSide.BUY) ? calculateOrderVolume(m_oTradeInfo.getNeedSpendSum(), oNewPrice) : oGetOrder.getVolume());
		final Order oAddOrder = addOrder(oGetOrder.getSide(), oNewVolume, oNewPrice);
		m_oTradeInfo.setOrder(oAddOrder);
		
		if (oGetOrder.getSide().equals(OrderSide.BUY))
		{
			m_oTradeInfo.setNeedBoughtVolume(oAddOrder.getVolume());
			m_oTradeInfo.setTradeSum(oAddOrder.getSum().add(m_oTradeInfo.getSpendSum()));
		}
		getStockExchange().getRules().save();

		final String strMessage = "- " + oGetOrder.getInfoShort() + "\r\n+ " + oAddOrder.getInfo();
		sendMessage(MessageLevel.TRACE, strMessage);

		final String strHistory = oGetOrder.getSide() + " " + (oGetOrder.getPrice().compareTo(oNewPrice) < 0 ? "^ " : "v ") + MathUtils.toCurrencyString(oNewPrice) + "/" + MathUtils.toCurrencyStringEx(oNewVolume);
		m_oTradeInfo.addToHistory(strHistory);
	}

	protected Order removeOrder(final Order oGetOrder)
	{
		int nTryCount = 5;
		final String strMessage = "Cannot delete order\r\n" + oGetOrder.getInfoShort();
		Order oRemoveOrder = new Order(Order.ERROR, strMessage);
		while (nTryCount > 0)
		{
			oRemoveOrder = getStockSource().removeOrder(oGetOrder.getId());
			if (oRemoveOrder.isCanceled())
				return oRemoveOrder;

			final Order oCheckRemoveOrder = getStockSource().getOrder(oGetOrder.getId(), m_oRateInfo);
			if (oCheckRemoveOrder.isDone() || oCheckRemoveOrder.isCanceled())
				return oCheckRemoveOrder;
			
			try { Thread.sleep(100); }
			catch (InterruptedException e) { break; }
			nTryCount--;
		}

		sendMessage(MessageLevel.ERROR, strMessage);
		return oRemoveOrder;
	}

	protected Order addOrder(final OrderSide oOrderSide, final BigDecimal oVolume, final BigDecimal oPrice)
	{
		int nTryCount = 5;
		Order oAddOrder = Order.NULL;
		while (nTryCount > 0)
		{
			oAddOrder = getStockSource().addOrder(oOrderSide, m_oRateInfo, oVolume, oPrice);
			if (!oAddOrder.isError())
				return oAddOrder;
			
			try { Thread.sleep(100); }
			catch (InterruptedException e) { break; }
			nTryCount--;
		}
		
		sendMessage(MessageLevel.ERROR, "Can't create order\r\n" + oOrderSide + "/" + MathUtils.toCurrencyStringEx(oVolume) + "/" + MathUtils.toCurrencyString(oPrice) + "\r\n" + oAddOrder.getInfoShort());
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
			
			sendMessage(MessageLevel.ERROR, "Order " + oOrder.getState());
			m_oTradeInfo.addToHistory("Order " + oOrder.getState());
			
			supertaskDone(oOrder);
			return;
		}

		if (m_oTradeInfo.getTaskSide().equals(OrderSide.SELL))
		{
			final BigDecimal nSellVolume = m_oTradeInfo.getNeedSellVolume();
			final BigDecimal nDeltaSellSum = nSellVolume.multiply(oOrder.getPrice());
			m_oTradeInfo.addSell(TradeUtils.getWithoutCommision(nDeltaSellSum), nSellVolume);
				
			sendMessage(MessageLevel.TRADERESULT, m_oTradeInfo.getInfo());
			m_oTradeInfo.addToHistory(m_oTradeInfo.getInfo());

			supertaskDone(oOrder);
			return;
		}
		
		sendMessage(MessageLevel.DEBUG, getInfo(null) + " is executed");
		
		final BigDecimal nTradeCommision = TradeUtils.getCommisionValue(m_oTradeInfo.getAveragedBoughPrice(), BigDecimal.ZERO);
		final BigDecimal nTradeMargin = TradeUtils.getMarginValue(m_oTradeInfo.getAveragedBoughPrice());
		m_oTradeInfo.setCriticalPrice(m_oTradeInfo.getAveragedBoughPrice().add(nTradeCommision).add(nTradeMargin));
		final String strMessage = "Set critical price " + m_oTradeInfo.getCriticalPriceString() + 
									"/" + MathUtils.toCurrencyString(nTradeCommision.multiply(new BigDecimal(2))) + 
									"/" + MathUtils.toCurrencyString(nTradeMargin);
		sendMessage(MessageLevel.DEBUG, strMessage);
		m_oTradeInfo.addToHistory(strMessage);
		getTradeControler().buyDone(this);

		m_oTradeInfo.setOrder(Order.NULL);
		m_oTradeInfo.setTaskSide(OrderSide.SELL);
	}

	protected void supertaskDone(final Order oOrder)
	{
		if (getTradeControler().equals(ITradeControler.NULL))
			sendMessage(MessageLevel.TRADERESULT, "Task done. " + getInfo(null) + "\r\n" + m_oTradeInfo.getHistory());

		getTradeControler().tradeDone(this);
		getStockExchange().getRules().removeRule(this);
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
			final Order oGetOrder = getStockSource().getOrder(strValue, m_oRateInfo);
			if (!oGetOrder.isNull() && !oGetOrder.isError() && !oGetOrder.isCanceled())
				m_oTradeInfo.setOrder(oGetOrder);
		}
				
		super.setParameter(strParameterName, strValue);
	}
}


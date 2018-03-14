package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.source.IStockSource;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;
import solo.utils.MathUtils;

public class TaskTrade extends TaskBase implements ITradeTask
{
	public static final String NAME = "TRADE";

	private static final long serialVersionUID = -178132243757975169L;

	final static public String TRADE_VOLUME = "#volume#";
	final static public String CRITICAL_PRICE_PARAMETER = "criticalPrice";
	
	protected String m_strCurrentState = StringUtils.EMPTY;

	protected TradeInfo m_oTradeInfo;

	public TaskTrade(final String strCommandLine) throws Exception
	{
		this(strCommandLine, TRADE_VOLUME);
	}

	public TaskTrade(final String strCommandLine, final String strTemplate) throws Exception
	{
		super(strCommandLine, strTemplate);
		getTradeInfo().setTradeSum(getParameterAsBigDecimal(TRADE_VOLUME), true);
		getTradeInfo().setTaskSide(OrderSide.BUY);
	}

	public TradeInfo getTradeInfo()
	{
		if (null == m_oTradeInfo)
			m_oTradeInfo = new TradeInfo(m_oRateInfo, WorkerFactory.getStockExchange().getRules().getNextRuleID());
		
		return m_oTradeInfo;
	}

	@Override public String getType()
	{
		return NAME;   
	}
	
	public void starTask() throws Exception
	{
		getTradeControler().tradeStart(this);
	}
	
	public String getInfo()
	{
		return (getTradeControler().equals(ITradeControler.NULL) ? getType() + " " : StringUtils.EMPTY) + 
					(getTradeInfo().getOrder().equals(Order.NULL) ?  getTradeInfo().getTaskSide() + " " : StringUtils.EMPTY) + 
					getTradeInfo().getOrder().getInfoShort() + "\r\n" + 
					(StringUtils.isNotBlank(m_strCurrentState) ? "State [" + m_strCurrentState + "]\r\n" : StringUtils.EMPTY);   
	}
	
	protected IStockSource getStockSource()
	{
		return WorkerFactory.getStockSource(this);
	}

	@Override public void check(final StateAnalysisResult oStateAnalysisResult)
	{
		m_strCurrentState = StringUtils.EMPTY;

		final Order oGetOrder = updateTradeInfo(getTradeInfo().getOrder());
		checkTaskDone(oGetOrder);

		final RateAnalysisResult oRateAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
		if (getTradeInfo().getTaskSide().equals(OrderSide.BUY) && getTradeInfo().getOrder().isNull())
		{
			BigDecimal oBuyPrice = getTradeInfo().getBuyStrategy().getBuyPrice(oRateAnalysisResult, getTradeInfo());
			if (oBuyPrice.equals(BigDecimal.ZERO))
				return;
			
			getTradeInfo().setOrder(createBuyOrder(oBuyPrice));
			WorkerFactory.getStockExchange().getRules().save();
			return;
		}

		if (getTradeInfo().getTaskSide().equals(OrderSide.SELL) && getTradeInfo().getOrder().isNull())
		{
			BigDecimal oSellPrice = getTradeInfo().getSellStrategy().getSellPrice(oRateAnalysisResult, getTradeInfo());
			if (oSellPrice.equals(BigDecimal.ZERO))
				return;
			
			getTradeInfo().setOrder(createSellOrder(oSellPrice));
			WorkerFactory.getStockExchange().getRules().save();
			return;
		}
		
		if (OrderSide.BUY.equals(oGetOrder.getSide()))
		{
			final BigDecimal oBuyPrice = getTradeInfo().getBuyStrategy().getBuyPrice(oRateAnalysisResult, getTradeInfo());
			setNewOrderPrice(oBuyPrice, oGetOrder);
			return;
		}

		if (OrderSide.SELL.equals(oGetOrder.getSide()))
		{
			BigDecimal oSellPrice = getTradeInfo().getSellStrategy().getSellPrice(oRateAnalysisResult, getTradeInfo());
			oSellPrice = getTradeInfo().trimSellPrice(oSellPrice);
			setNewOrderPrice(oSellPrice, oGetOrder);
			return;
		}
	}

	protected Order updateTradeInfo(final Order oOrder)
	{
		if (oOrder.isNull())
			return oOrder;
		
		final Order oGetOrder = getStockSource().getOrder(getTradeInfo().getOrder().getId(), m_oRateInfo);
		if (oGetOrder.isNull() || oGetOrder.isError())
		{
			m_strCurrentState = "updateTradeInfo - " + oGetOrder;
			return oOrder;
		}

		if (oGetOrder.isCanceled())
			return oGetOrder;
		
		getTradeInfo().setOrder(oGetOrder);
		updateOrderTradeInfo(oGetOrder);
		return oGetOrder;
	}

	public void updateOrderTradeInfo(final Order oGetOrder)
	{
		if (OrderSide.BUY.equals(oGetOrder.getSide()))
			updateBuyTradeInfo(oGetOrder);
		else 
		if (OrderSide.SELL.equals(oGetOrder.getSide()))
			updateSellTradeInfo(oGetOrder);
	}

	protected void updateBuyTradeInfo(final Order oGetOrder)
	{
		if (null == oGetOrder.getPrice())
			return;
		
		if (oGetOrder.isDone())
		{
			final BigDecimal nNeedBuyVolume = getTradeInfo().getNeedBoughtVolume();
			final BigDecimal nBuyVolume = TradeUtils.getWithoutCommision(nNeedBuyVolume);
			final BigDecimal nDeltaSpendSum = nNeedBuyVolume.multiply(oGetOrder.getPrice());
			
			getTradeInfo().addBuy(nDeltaSpendSum, nBuyVolume);
			getTradeInfo().setNeedBoughtVolume(BigDecimal.ZERO, true);
		}
		else
		{
			final BigDecimal nDeltaSpendSum = getTradeInfo().getNeedSpendSum().add(oGetOrder.getSum().negate());
			if (nDeltaSpendSum.compareTo(BigDecimal.ZERO) == 0)
				return;

			final BigDecimal nDeltaBoughtVolume = getTradeInfo().getNeedBoughtVolume().add(oGetOrder.getVolume().negate());
			getTradeInfo().addBuy(nDeltaSpendSum, TradeUtils.getWithoutCommision(nDeltaBoughtVolume));
			getTradeInfo().setNeedBoughtVolume(oGetOrder.getVolume(), true);
		}
		WorkerFactory.getStockExchange().getRules().save();
	}

	protected void updateSellTradeInfo(final Order oGetOrder)
	{
		if (null == oGetOrder.getPrice())
			return;
		
		if (oGetOrder.isDone())
		{
			final BigDecimal nSellVolume = getTradeInfo().getNeedSellVolume();
			final BigDecimal nDeltaSellSum = nSellVolume.multiply(oGetOrder.getPrice());
			getTradeInfo().addSell(TradeUtils.getWithoutCommision(nDeltaSellSum), nSellVolume);
		}
		else
		{
			final BigDecimal nDeltaSellVolume = getTradeInfo().getNeedSellVolume().add(oGetOrder.getVolume().negate());
			final BigDecimal nDeltaSellSum = nDeltaSellVolume.multiply(oGetOrder.getPrice());
			if (nDeltaSellSum.compareTo(BigDecimal.ZERO) == 0)
				return;
			
			getTradeInfo().addSell(TradeUtils.getWithoutCommision(nDeltaSellSum), nDeltaSellVolume);
		}
		WorkerFactory.getStockExchange().getRules().save();
	}

	protected Order createBuyOrder(BigDecimal oBuyPrice)
	{
		final BigDecimal oVolume = calculateOrderVolume(getTradeInfo().getNeedSpendSum(), oBuyPrice);
		final Order oBuyOrder = addOrder(OrderSide.BUY, oVolume, oBuyPrice);
		if (oBuyOrder.isNull())
			return oBuyOrder;
		
		getTradeInfo().setNeedBoughtVolume(oBuyOrder.getVolume(), true);
		getTradeInfo().setTradeSum(oBuyOrder.getSum().add(getTradeInfo().getSpendSum()), true);
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, "Create " + oBuyOrder.getInfo());
		getTradeInfo().addToHistory(oBuyOrder.getSide() + " / " + MathUtils.toCurrencyStringEx3(oBuyOrder.getPrice()) + " / " + MathUtils.toCurrencyStringEx3(oBuyOrder.getSum()));
		return oBuyOrder;
	}

	protected Order createSellOrder(BigDecimal oSellPrice)
	{
		final BigDecimal oSellOrderPrice = getTradeInfo().trimSellPrice(oSellPrice); 
		BigDecimal oSellOrderVolume = getTradeInfo().getNeedSellVolume(); 
		oSellOrderVolume = TradeUtils.getRoundedVolume(m_oRateInfo, oSellOrderVolume); 
		Order oSellOrder = addOrder(OrderSide.SELL, oSellOrderVolume, oSellOrderPrice);
		if (oSellOrder.isNull())
			return oSellOrder;

		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, "Create " + oSellOrder.getInfo() + "/" + getTradeInfo().getCriticalPriceString());
		getTradeInfo().addToHistory(oSellOrder.getSide() + " / " + MathUtils.toCurrencyStringEx3(oSellOrder.getPrice()) + " / " + getTradeInfo().getCriticalPriceString());
		return oSellOrder;
	}
	
	protected void setNewOrderPrice(final BigDecimal oNewPrice, final Order oGetOrder)
	{
		final BigDecimal nRoundedNewPrice = TradeUtils.getRoundedPrice(m_oRateInfo, oNewPrice);
		final BigDecimal nRoundedOrderPrice = TradeUtils.getRoundedPrice(m_oRateInfo, oGetOrder.getPrice());
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
		final Order oRemoveOrder = TradeUtils.removeOrder(oGetOrder, getRateInfo(), getStockSource());
		if (oRemoveOrder.isException())
			return;
		
		if (oRemoveOrder.isDone())
		{
			getTradeInfo().setOrder(oRemoveOrder);
			WorkerFactory.getStockExchange().getRules().save();
			return;
		}
		
		if (oRemoveOrder.isCanceled())
		{
			if (OrderSide.SELL.equals(oRemoveOrder.getSide()) && null != oRemoveOrder.getVolume())
			{
				final BigDecimal nDeltaSellVolume = getTradeInfo().getNeedSellVolume().add(oRemoveOrder.getVolume().negate());
				if (nDeltaSellVolume.compareTo(BigDecimal.ZERO) > 0)
					getTradeInfo().getHistory().addToHistory("nDeltaSellVolume on cancel volume [" + nDeltaSellVolume + "]. Remove order " + oRemoveOrder.getInfoShort());
			}
			updateOrderTradeInfo(oRemoveOrder);
		}
		
		final BigDecimal oNewVolume = (oGetOrder.getSide().equals(OrderSide.BUY) ? calculateOrderVolume(getTradeInfo().getNeedSpendSum(), oNewPrice) : getTradeInfo().getNeedSellVolume());
		if (oNewVolume.compareTo(nMinTradeVolume) < 0)
		{
			getTradeInfo().getHistory().addToHistory("Volume is small [" + oNewVolume + "]. Remove order " + oRemoveOrder.getInfoShort());
			getTradeInfo().setOrder(oRemoveOrder);
			WorkerFactory.getStockExchange().getRules().save();
			return;
		}

		final Order oAddOrder = addOrder(oGetOrder.getSide(), oNewVolume, oNewPrice);
		if (oAddOrder.isError() || oAddOrder.isException())
		{
			getTradeInfo().getHistory().addToLog("Can't add order " + oAddOrder.getInfoShort());
			getTradeInfo().setOrder(Order.NULL);
			WorkerFactory.getStockExchange().getRules().save();
			return;
		}
		
		if (null != oOrderDateCreate)
			oAddOrder.setCreated(oOrderDateCreate);
		getTradeInfo().setOrder(oAddOrder);
		
		if (oGetOrder.getSide().equals(OrderSide.BUY))
		{
			getTradeInfo().setNeedBoughtVolume(oAddOrder.getVolume(), false);
			getTradeInfo().setTradeSum(oAddOrder.getSum().add(getTradeInfo().getSpendSum()), false);
		}

		final String strMessage = "- " + oGetOrder.getInfoShort() + "\r\n+ " + oAddOrder.getInfo();
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.TRACE, strMessage);
		WorkerFactory.getStockExchange().getRules().save();
		
		String strLogMessage = oAddOrder.getInfoShort(); 
		final RateAnalysisResult oAnalysisResult = WorkerFactory.getStockExchange().getLastAnalysisResult().getRateAnalysisResult(m_oRateInfo);
		final List<Order> oOrders = (oGetOrder.getSide().equals(OrderSide.BUY) ? oAnalysisResult.getBidsOrders() : oAnalysisResult.getAsksOrders());
		final List<Order> oTrades = oAnalysisResult.getTrades();
		strLogMessage += "\t-\t" + MathUtils.toCurrencyStringEx2(oOrders.get(0).getPrice()) + ";\t" + 
				(oOrders.size() > 1 ? MathUtils.toCurrencyStringEx2(oOrders.get(1).getPrice()) + ";\t" : StringUtils.EMPTY) + 
				(oOrders.size() > 2 ? MathUtils.toCurrencyStringEx2(oOrders.get(2).getPrice()) : StringUtils.EMPTY);
		strLogMessage += "\t-\t" + MathUtils.toCurrencyStringEx2(oTrades.get(0).getPrice()) + ";\t" + 
				(oTrades.size() > 1 ? MathUtils.toCurrencyStringEx2(oTrades.get(1).getPrice()) + ";\t" : StringUtils.EMPTY) + 
				(oTrades.size() > 2 ? MathUtils.toCurrencyStringEx2(oTrades.get(2).getPrice()) : StringUtils.EMPTY);
		getTradeInfo().getHistory().addToLog(strLogMessage + "\r\n");
	}

	protected Order addOrder(final OrderSide oOrderSide, final BigDecimal oVolume, final BigDecimal oPrice)
	{
		int nTryCount = 50;
		Order oAddOrder = Order.NULL;
		while (nTryCount > 0)
		{
			oAddOrder = getStockSource().addOrder(oOrderSide, m_oRateInfo, oVolume, oPrice);
			if (!oAddOrder.isException())
				return oAddOrder;
			
			try { Thread.sleep(100); }
			catch (InterruptedException e) { break; }
			nTryCount--;
		}
		
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.TRACE, "Can't create order\r\n" + oOrderSide + "/" + MathUtils.toCurrencyStringEx2(oVolume) + "/" + MathUtils.toCurrencyStringEx2(oPrice) + "\r\n" + oAddOrder.getInfoShort());
		m_strCurrentState = "addOrder - " + oAddOrder.getInfoShort();
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
		
		if (!oGetOrder.isCanceled() && !oGetOrder.isDone())
			return;
		
		if (oGetOrder.isCanceled())
		{
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, "Order cancel. " + oGetOrder.getInfoShort());
			getTradeInfo().addToHistory("Order cancel. " + oGetOrder.getInfoShort());
			
			if (getTradeInfo().getTaskSide().equals(OrderSide.BUY))
			{
				buyDone(oGetOrder);
				return;
			}
			
			taskDone(oGetOrder);
			return;
		}

		if (getTradeInfo().getTaskSide().equals(OrderSide.SELL))
			sellDone(oGetOrder);
		else
			buyDone(oGetOrder);
	}

	protected void buyDone(final Order oOrder)
	{
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, getInfo() + " is executed");
		
		final String strMessage = "/" + MathUtils.toCurrencyStringEx2(TradeUtils.getCommisionValue(getTradeInfo().getAveragedBoughPrice(), getTradeInfo().getAveragedBoughPrice())) + 
									"/" + MathUtils.toCurrencyStringEx2(TradeUtils.getMarginValue(getTradeInfo().getAveragedBoughPrice(), getTradeInfo().getRateInfo()));

		getTradeInfo().setCriticalPrice(getTradeInfo().calculateCriticalPrice(), strMessage);
		getTradeControler().buyDone(this);

		getTradeInfo().setOrder(Order.NULL);
		getTradeInfo().setTaskSide(OrderSide.SELL);
		WorkerFactory.getStockExchange().getRules().save();
	}

	protected void sellDone(final Order oOrder)
	{
		final BigDecimal nSellVolume = getTradeInfo().getNeedSellVolume();
		final BigDecimal nDeltaSellSum = nSellVolume.multiply(oOrder.getPrice());
		getTradeInfo().addSell(TradeUtils.getWithoutCommision(nDeltaSellSum), nSellVolume);
		getTradeInfo().addToHistory(getTradeInfo().getInfo());

		taskDone(oOrder);
	}

	protected void taskDone(final Order oOrder)
	{
		if (getTradeControler().equals(ITradeControler.NULL))
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.TRADERESULT, "Task done. " + getInfo() + "\r\n" + getTradeInfo().getHistory());

		getTradeControler().tradeDone(this);
		WorkerFactory.getStockExchange().getRules().removeRule(this);
	}
	
	public void remove()
	{
		setTradeControler(ITradeControler.NULL);
		if (!getTradeInfo().getOrder().isNull())
			getStockSource().removeOrder(getTradeInfo().getOrder().getId(), null);
	}
	
	@Override public ITradeControler getTradeControler()
	{
		return getTradeInfo().getTradeControler();
	}

	public void setTradeControler(final ITradeControler oTradeControler)
	{
		getTradeInfo().setTradeControler(oTradeControler);
	}
	
	@Override public void setParameter(final String strParameterName, final String strValue)
	{
		if (strParameterName.equalsIgnoreCase("criticalPrice"))
			getTradeInfo().setCriticalPrice(MathUtils.fromString(strValue));

		if (strParameterName.equalsIgnoreCase("addSellVolume"))
			getTradeInfo().addSell(BigDecimal.ZERO, MathUtils.fromString(strValue));
				
		super.setParameter(strParameterName, strValue);
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		return  m_oRateInfo + "\r\n" + getTradeInfo().getInfo() + 
			(StringUtils.isNotBlank(m_strCurrentState) ? "\r\nState : [" + m_strCurrentState + "]" : StringUtils.EMPTY);
	}
}


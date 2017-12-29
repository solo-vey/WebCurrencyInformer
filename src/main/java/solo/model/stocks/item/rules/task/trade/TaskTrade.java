package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.trade.RemoveOrderCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
import ua.lz.ep.utils.ResourceUtils;

public class TaskTrade extends TaskBase implements ITradeTask
{
	private static final long serialVersionUID = -178132243757975169L;

	final static public String TRADE_VOLUME = "#volume#";

	protected TradeInfo m_oTradeInfo;
	protected ITradeControler m_oTradeControler = ITradeControler.NULL;

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
			" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo.getCurrencyFrom().toString().toLowerCase()) +   
			" " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, getStockExchange().getRules().getRuleID(this)); 
		super.sendMessage(strMessage + "\r\n" + strMessageFooter);
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return getType() + "/" + (m_oTradeInfo.getOrder().equals(Order.NULL) ?  m_oTradeInfo.getTaskSide() + "/" : StringUtils.EMPTY) + 
					m_oTradeInfo.getOrder().getInfoShort() + "\r\n" + 
					CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo.getCurrencyFrom().toString().toLowerCase()) +   
					" " + CommandFactory.makeCommandLine(RemoveOrderCommand.class, RemoveOrderCommand.ID_PARAMETER, m_oTradeInfo.getOrder().getId()) + 
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
		
		if (oGetOrder.getSide().equals(OrderSide.BUY))
		{
			if (oGetOrder.isDone())
			{
				m_oTradeInfo.addBoughtVolume(TradeUtils.getWithoutCommision(m_oTradeInfo.getNeedBoughtVolume()));
				final BigDecimal nDeltaSpendSum = m_oTradeInfo.getNeedBoughtVolume().multiply(oOrder.getPrice());
				m_oTradeInfo.addSpendSum(nDeltaSpendSum);
				m_oTradeInfo.setNeedBoughtVolume(BigDecimal.ZERO);
			}
			else
			{
				final BigDecimal nDeltaSpendSum = m_oTradeInfo.getNeedSpendSum().add(oGetOrder.getSum().negate());
				if (nDeltaSpendSum.compareTo(BigDecimal.ZERO) == 0)
					return oGetOrder;
	
				m_oTradeInfo.addSpendSum(nDeltaSpendSum);
				final BigDecimal nDeltaBoughtVolume = m_oTradeInfo.getNeedBoughtVolume().add(oGetOrder.getVolume().negate());
				m_oTradeInfo.addBoughtVolume(TradeUtils.getWithoutCommision(nDeltaBoughtVolume));
				m_oTradeInfo.setNeedBoughtVolume(oGetOrder.getVolume());
			}
			return oGetOrder;
		}

		if (oGetOrder.getSide().equals(OrderSide.SELL))
		{
			if (oGetOrder.isDone())
			{
				final BigDecimal nDeltaSellSum = m_oTradeInfo.getNeedSellVolume().multiply(oOrder.getPrice());
				m_oTradeInfo.addReceivedSum(TradeUtils.getWithoutCommision(nDeltaSellSum));
				m_oTradeInfo.addSoldVolume(m_oTradeInfo.getNeedSellVolume());
			}
			else
			{
				final BigDecimal nDeltaSellVolume = m_oTradeInfo.getNeedSellVolume().add(oGetOrder.getVolume().negate());
				final BigDecimal nDeltaSellSum = nDeltaSellVolume.multiply(oGetOrder.getPrice());
				if (nDeltaSellVolume.compareTo(BigDecimal.ZERO) == 0 || nDeltaSellSum.compareTo(BigDecimal.ZERO) == 0)
					return oGetOrder;
				
				m_oTradeInfo.addSoldVolume(nDeltaSellVolume);
				m_oTradeInfo.addReceivedSum(TradeUtils.getWithoutCommision(nDeltaSellSum));
			}
			return oGetOrder;
		}
		
		return oGetOrder;
	}

	protected Order createBuyOrder(BigDecimal oBuyPrice)
	{
		final BigDecimal oVolume = calculateOrderVolume(m_oTradeInfo.getNeedSpendSum(), oBuyPrice);
		Order oBuyOrder = getStockSource().addOrder(OrderSide.BUY, m_oRateInfo, oVolume, oBuyPrice);
		if (oBuyOrder.isNull() || oBuyOrder.isError())
		{
			sendMessage("Cannot create " + oBuyOrder.getInfo());
			final Order oLookOrder = lookForOrder(OrderSide.BUY, oVolume, oBuyPrice);
			if (oLookOrder.isNull())
				return Order.NULL;
			oBuyOrder = oLookOrder;
		}
		
		m_oTradeInfo.setNeedBoughtVolume(oBuyOrder.getVolume());
		m_oTradeInfo.setTradeSum(oBuyOrder.getSum());
		sendMessage("Create " + oBuyOrder.getInfo());
		m_oTradeInfo.addToHistory(oBuyOrder.getSide() + " + " + MathUtils.toCurrencyString(oBuyOrder.getPrice()));
		getStockExchange().getRules().save();
		
		return oBuyOrder;
	}

	protected Order createSellOrder(BigDecimal oSellPrice)
	{
		final BigDecimal oSellOrderPrice = m_oTradeInfo.trimSellPrice(oSellPrice); 
		BigDecimal oSellOrderVolume = m_oTradeInfo.getNeedSellVolume(); 
		oSellOrderVolume = TradeUtils.getRoundedVolume(m_oRateInfo, oSellOrderVolume); 
		Order oSellOrder = getStockSource().addOrder(OrderSide.SELL, m_oRateInfo, oSellOrderVolume, oSellOrderPrice);
		if (oSellOrder.isNull() || oSellOrder.isError())
		{
			final Order oLookOrder = lookForOrder(OrderSide.SELL, oSellOrderVolume, oSellOrderPrice);
			if (oLookOrder.isNull())
				return Order.NULL;
			oSellOrder = oLookOrder;
		}

		sendMessage("Create " + oSellOrder.getInfo() + "/" + m_oTradeInfo.getCriticalPriceString());
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

		final String strMarket = m_oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + m_oRateInfo.getCurrencyTo().toString().toLowerCase(); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strMinVolume = ResourceUtils.getResource("stock." + strMarket + ".min_volume", oStockExchange.getStockProperties(), "0.000001");
		final BigDecimal nMinTradeVolume = MathUtils.getBigDecimal(Double.parseDouble(strMinVolume), TradeUtils.getVolumePrecision(m_oRateInfo));
		if (oGetOrder.getVolume().compareTo(nMinTradeVolume) < 0)
			return;

		String strMessage = StringUtils.EMPTY; 
		final Order oRemoveOrder = getStockSource().removeOrder(oGetOrder.getId());
		if (oRemoveOrder.isNull() || oRemoveOrder.isError())
			sendMessage("Cannot delete order\r\n" + oGetOrder.getInfoShort() + "\r\n" + oRemoveOrder.getInfoShort());
		
		strMessage += "- " + oGetOrder.getInfoShort() + "\r\n"; 
		final BigDecimal oNewVolume = (oGetOrder.getSide().equals(OrderSide.BUY) ? calculateOrderVolume(m_oTradeInfo.getNeedSpendSum(), oNewPrice) : oGetOrder.getVolume());
		Order oAddOrder = getStockSource().addOrder(oGetOrder.getSide(), m_oRateInfo, oNewVolume, oNewPrice);
		if (oAddOrder.isNull() || oAddOrder.isError())
		{
			final Order oLookOrder = lookForOrder(oGetOrder.getSide(), oNewVolume, oNewPrice);
			if (oLookOrder.isNull())
			{
				sendMessage("Cannot recreate order\r\n" + oGetOrder.getSide() + "/" + MathUtils.toCurrencyStringEx(oNewVolume) + "/" + MathUtils.toCurrencyString(oNewPrice) + "\r\n" + oAddOrder.getInfoShort());
				m_oTradeInfo.setOrder(Order.NULL);
				return;
			}
			oAddOrder = oLookOrder;
		}

		m_oTradeInfo.setOrder(oAddOrder);
		if (oGetOrder.getSide().equals(OrderSide.BUY))
		{
			m_oTradeInfo.setNeedBoughtVolume(oAddOrder.getVolume());
			m_oTradeInfo.setTradeSum(oAddOrder.getSum().add(m_oTradeInfo.getSpendSum()));
		}

		strMessage += "+ " + oAddOrder.getInfo();
		
		getStockExchange().getRules().save();
//		sendMessage(strMessage);

		final String strHistory = oGetOrder.getSide() + " " + (oGetOrder.getPrice().compareTo(oNewPrice) < 0 ? "^ " : "v ") + MathUtils.toCurrencyString(oNewPrice) + "/" + MathUtils.toCurrencyStringEx(oNewVolume);
		m_oTradeInfo.addToHistory(strHistory);
	}

	protected Order lookForOrder(OrderSide oSide, BigDecimal oVolume, BigDecimal oPrice)
	{
		final StockUserInfo oUserInfo = getStockSource().getUserInfo(m_oRateInfo);
		final List<Order> oOrders = oUserInfo.getOrders(m_oRateInfo);
		for(final Order oOrder : oOrders)
		{
			final BigDecimal oVolumeOnePersent = oVolume.divide(new BigDecimal(100));
			if (oOrder.getPrice().compareTo(oPrice) == 0 && oOrder.getSide().equals(oSide) && 
					oVolume.add(oOrder.getVolume().negate()).compareTo(oVolumeOnePersent) < 0)
				return oOrder;
		}
		
		return Order.NULL;
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
			
			sendMessage("Order " + oOrder.getState());
			m_oTradeInfo.addToHistory("Order " + oOrder.getState());
			
			supertaskDone(oOrder);
			return;
		}

		if (m_oTradeInfo.getTaskSide().equals(OrderSide.SELL))
		{
			m_oTradeInfo.addSoldVolume(m_oTradeInfo.getNeedSellVolume());
			final BigDecimal nDeltaSellSum = m_oTradeInfo.getNeedSellVolume().multiply(oOrder.getPrice());
			m_oTradeInfo.addReceivedSum(TradeUtils.getWithoutCommision(nDeltaSellSum));
				
			sendMessage(m_oTradeInfo.getInfo());
			m_oTradeInfo.addToHistory(m_oTradeInfo.getInfo());

			supertaskDone(oOrder);
			return;
		}
		
		sendMessage(getInfo(null) + " is executed");
		
		final BigDecimal nTradeCommision = TradeUtils.getCommisionValue(m_oTradeInfo.getAveragedBoughPrice(), BigDecimal.ZERO);
		final BigDecimal nTradeMargin = TradeUtils.getMarginValue(m_oTradeInfo.getAveragedBoughPrice());
		m_oTradeInfo.setCriticalPrice(m_oTradeInfo.getAveragedBoughPrice().add(nTradeCommision).add(nTradeMargin));
		final String strMessage = "Set critical price " + m_oTradeInfo.getCriticalPriceString() + 
									"/" + MathUtils.toCurrencyString(nTradeCommision.multiply(new BigDecimal(2))) + 
									"/" + MathUtils.toCurrencyString(nTradeMargin);
		sendMessage(strMessage);
		m_oTradeInfo.addToHistory(strMessage);

		m_oTradeInfo.setOrder(Order.NULL);
		m_oTradeInfo.setTaskSide(OrderSide.SELL);
	}

	protected void supertaskDone(final Order oOrder)
	{
		sendMessage("Task done. " + getInfo(null));
		super.sendMessage(m_oTradeInfo.getHistory());
		getTradeControler().tradeDone(this);
		getStockExchange().getRules().removeRule(this);
	}
	
	@Override public ITradeControler getTradeControler()
	{
		return m_oTradeControler;
	}

	public void setTradeControler(final ITradeControler oTradeControler)
	{
		m_oTradeControler = oTradeControler;
	}
}


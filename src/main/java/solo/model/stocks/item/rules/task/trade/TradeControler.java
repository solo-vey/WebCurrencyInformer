package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import solo.model.currency.CurrencyAmount;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RulesFactory;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.item.analyse.CandlestickType;
import solo.model.stocks.item.analyse.StockCandlestick;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.trade.GetTradeInfoCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.item.rules.task.TaskFactory;
import solo.model.stocks.item.rules.task.TaskType;
import solo.model.stocks.item.rules.task.strategy.IBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickBuyExStrategy;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.item.rules.task.strategy.trade.ITradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.SimpleTradeStrategy;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TradeControler extends TaskBase implements ITradeControler
{
	private static final long serialVersionUID = 2548242566461334806L;
	
	final static public String TRADE_SUM = "#sum#";
	final static public String MAX_TARDES = "#count#";
	
	protected Integer m_nMaxTrades = 2;
	protected Map<RateInfo, TradesInfo> m_oAllTradesInfo = new HashMap<RateInfo, TradesInfo>();
	protected TradesInfo m_oTradesInfo;
	protected IBuyStrategy m_oBuyStrategy = null;
	protected ITradeStrategy m_oTradeStrategy = null;
	protected String m_strCurrentState = StringUtils.EMPTY;

	public TradeControler(RateInfo oRateInfo, String strCommandLine)
	{
		super(oRateInfo, strCommandLine, CommonUtils.mergeParameters(TRADE_SUM, MAX_TARDES));
		m_nMaxTrades = getParameterAsInt(MAX_TARDES);
		m_nMaxTrades = (m_nMaxTrades.equals(Integer.MIN_VALUE) ? 2 : m_nMaxTrades);
		final BigDecimal nTradeSum = getParameterAsBigDecimal(TRADE_SUM);
		getTradesInfo().setSum(nTradeSum, m_nMaxTrades);
	}
	
	public TradeControler(final RateInfo oRateInfo, final String strCommandLine, final String strTemplate)
	{
		super(oRateInfo, strCommandLine, CommonUtils.mergeParameters(TRADE_SUM, MAX_TARDES, strTemplate));
		m_nMaxTrades = getParameterAsInt(MAX_TARDES);
		m_nMaxTrades = (m_nMaxTrades.equals(Integer.MIN_VALUE) ? 2 : m_nMaxTrades);
		final BigDecimal nTradeSum = getParameterAsBigDecimal(TRADE_SUM);
		getTradesInfo().setSum(nTradeSum, m_nMaxTrades);
	}
	
	public RateInfo getRateInfo()
	{
		return getTradesInfo().getRateInfo();
	}
	
	public String getFullInfo()
	{ 
		int nRuleID = getRuleID(this);
		String strInfo = getRateInfo().toString().toUpperCase();
		strInfo += " / buyStrategy [" + (null != getBuyStrategy() ? getBuyStrategy().getName() : "Default") + "]";
		strInfo += " / tradeCount [" + m_nMaxTrades + "]\r\n";
		return getTradesInfo().getInfo() + "\r\n" + strInfo + 
				CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, nRuleID, GetTradeInfoCommand.FULL_PARAMETER, "true");
	}
	
	public IBuyStrategy getBuyStrategy()
	{
		return m_oBuyStrategy;
	}
	
	public ITradeStrategy getTradeStrategy()
	{
		if (null == m_oTradeStrategy)
			m_oTradeStrategy = StrategyFactory.getTradeStrategy(SimpleTradeStrategy.NAME);
		return m_oTradeStrategy;
	}
	
	@Override public String getType()
	{
		return "CONTROLER";   
	}
	
	public TradesInfo getTradesInfo()
	{
		if (null == m_oTradesInfo)
			m_oTradesInfo = new TradesInfo(m_oRateInfo);
		return m_oTradesInfo;   
	}
	
	public Map<RateInfo, TradesInfo> getAllTradesInfo()
	{
		if (null == m_oAllTradesInfo || m_oAllTradesInfo.size() == 0)
		{
			m_oAllTradesInfo = new HashMap<RateInfo, TradesInfo>();
			m_oAllTradesInfo.put(getRateInfo(), new TradesInfo(getRateInfo()));
			
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(getRateInfo());
			m_oAllTradesInfo.put(oReverseRateInfo, new TradesInfo(oReverseRateInfo));
		}
		return m_oAllTradesInfo;   
	}
	
	public String getInfo(final Integer nRuleID)
	{
		String strInfo = getType() + 
				" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, getRateInfo()) + 
				" " + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, nRuleID, GetTradeInfoCommand.FULL_PARAMETER, StringUtils.EMPTY) + 
				"\r\n";   
		final List<ITradeTask> aTaskTrades = getTaskTrades();
		for(final ITradeTask oTaskTrade : aTaskTrades)
		{
			int nTaskRuleID = getRuleID(oTaskTrade);
			strInfo += " -> " + oTaskTrade.getInfo(nTaskRuleID).replace("\r\n", "\r\n    ") + "\r\n"; 
		}
		if (StringUtils.isNotBlank(m_strCurrentState))
			strInfo += "State [" + m_strCurrentState + "]\r\n"; 

		return strInfo +  
				(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}
	
	protected int getRuleID(final IRule oTask)
	{
		for(final IRule oRule : WorkerFactory.getStockExchange().getRules().getRules().values())
		{
			final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRule);
			if (null != oTradeTask && oTradeTask.equals(oTask))
				return WorkerFactory.getStockExchange().getRules().getRuleID(oRule);	

			final ITradeControler oTradeControler = TradeUtils.getRuleAsTradeControler(oRule);
			if (null != oTradeControler && oTradeControler.equals(oTask))
				return WorkerFactory.getStockExchange().getRules().getRuleID(oRule);	
		}
		
		return -1;
	}
	
	protected List<ITradeTask> getTaskTrades()
	{
		final List<ITradeTask> aTaskTrades = new LinkedList<ITradeTask>();
		final Map<Integer, IRule> oRules = WorkerFactory.getStockExchange().getRules().getRules();
		for(final IRule oRule : oRules.values())
		{
			final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRule);
			if (null == oTradeTask)
				continue;
			
			if (this.equals(oTradeTask.getTradeControler()))
				aTaskTrades.add(oTradeTask);	
		}
		return aTaskTrades;
	}
	
	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		m_strCurrentState = StringUtils.EMPTY;
		final List<ITradeTask> aTaskTrades = getTaskTrades();
		for(final ITradeTask oTaskTrade : aTaskTrades)
			oTaskTrade.check(oStateAnalysisResult, nRuleID);
		
		m_oTradesInfo.updateOrderInfo(aTaskTrades);
		
		boolean bIsBuyPrecent = false;
		for(final ITradeTask oTaskTrade : aTaskTrades)
			bIsBuyPrecent |= oTaskTrade.getTradeInfo().getTaskSide().equals(OrderSide.BUY);

		for(final ITradeTask oTaskTrade : aTaskTrades)
			checkTrade(oTaskTrade, bIsBuyPrecent, aTaskTrades);

		if (aTaskTrades.size() < m_nMaxTrades && !bIsBuyPrecent)
			createNewTrade(oStateAnalysisResult, aTaskTrades);
		
		changeReverseRateTrade(aTaskTrades);
	}
	
	protected void checkTrade(final ITradeTask oTaskTrade, boolean bIsBuyPrecent, List<ITradeTask> aTaskTrades)
	{
		if (bIsBuyPrecent || aTaskTrades.size() < m_nMaxTrades)
			return;
		
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(getRateInfo());
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
	    if (oOrder.getVolume().compareTo(nMinTradeVolume) < 0)
	    	return;
	    
		final Date oMaxDateCreate = DateUtils.addMinutes(new Date(), -15); 
	    if (null == oOrder.getCreated() || oOrder.getCreated().after(oMaxDateCreate))
	    	return;
		
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(getRateInfo());
		final CandlestickType oCandlestickType = oCandlestick.getType();
		if (!oCandlestickType.isCalm())
			return;
	    
    	final BigDecimal nNewCriticalPrice = oCandlestick.getAverageMaxPrice(3);
		final BigDecimal nMinCriticalPrice = oTaskTrade.getTradeInfo().getMinCriticalPrice();
    	if (nNewCriticalPrice.compareTo(nMinCriticalPrice) > 0)
    	{
    		oTaskTrade.getTradeInfo().setCriticalPrice(nNewCriticalPrice);
    		WorkerFactory.getMainWorker().sendMessage(getType() + "\r\n" + oTaskTrade.getInfo(null) + "\r\n" +
    				"Reset critical price " + MathUtils.toCurrencyString(nNewCriticalPrice));
    	}
	}
	
	protected void createNewTrade(final StateAnalysisResult oStateAnalysisResult, List<ITradeTask> aTaskTrades)
	{
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(getRateInfo());
		final CandlestickType oCandlestickType = oCandlestick.getType();
		if (oCandlestickType.isFall())
		{
			m_strCurrentState = "Wait buy trand - " + oCandlestickType;
			return;
		}
		
		try
		{
			final BigDecimal oMinTradeVolume = TradeUtils.getMinTradeVolume(getRateInfo());
			final BigDecimal oBuyPrice = oStateAnalysisResult.getRateAnalysisResult(getRateInfo()).getBidsOrders().get(0).getPrice();
			final BigDecimal oMinTradeSum = oMinTradeVolume.multiply(oBuyPrice).multiply(new BigDecimal(1.01)); 

			final BigDecimal nTotalSum = m_oTradesInfo.getSum().add(m_oTradesInfo.getSumToSell());
			if (nTotalSum.compareTo(oMinTradeSum) < 0)
			{
				m_strCurrentState = "Wait buy. No money - " + MathUtils.toCurrencyStringEx2(nTotalSum) + " < " + MathUtils.toCurrencyStringEx2(oMinTradeSum);
				return;
			}
				
			BigDecimal nBuySum = MathUtils.getRoundedBigDecimal(nTotalSum.doubleValue() / m_nMaxTrades, TradeUtils.getVolumePrecision(getRateInfo()));
			final CurrencyAmount oCurrencyAmount = WorkerFactory.getStockSource().getUserInfo(getRateInfo()).getMoney().get(getRateInfo().getCurrencyTo());
			if (null != oCurrencyAmount && oCurrencyAmount.getBalance().compareTo(nBuySum) < 0)
				nBuySum = oCurrencyAmount.getBalance();

			if (nBuySum.compareTo(oMinTradeSum) < 0)
			{
				nBuySum = oMinTradeSum;
				if (null != oCurrencyAmount && oCurrencyAmount.getBalance().compareTo(nBuySum) < 0)
				{
					m_strCurrentState = "Wait buy. No money on balance - " + MathUtils.toCurrencyStringEx2(nBuySum) + " < " + MathUtils.toCurrencyStringEx2(oCurrencyAmount.getBalance());
					return;
				}

			}
			
			final String strRuleInfo = "task" + "_" + getRateInfo() + "_" + TaskType.TRADE.toString().toLowerCase() + "_" + nBuySum;
			final TaskFactory oTask = (TaskFactory) RulesFactory.getRule(strRuleInfo);
			final TaskTrade oTaskTrade = ((TaskTrade)oTask.getTaskBase());
			
			if (m_oTradesInfo.getFreeVolume().compareTo(BigDecimal.ZERO) > 0)
			{
				final BigDecimal nNeedSellVolume = m_oTradesInfo.getFreeVolume();
				oTaskTrade.getTradeInfo().addBuy(BigDecimal.ZERO, nNeedSellVolume);
			}
			if (null != getBuyStrategy())
				oTaskTrade.getTradeInfo().setBuyStrategy(getBuyStrategy());
			
			if (getRateInfo().getIsReverse())
			{
				m_oBuyStrategy = StrategyFactory.getBuyStrategy(QuickBuyExStrategy.NAME);
				oTaskTrade.getTradeInfo().setCriticalVolume(BigDecimal.ZERO);
			}
			
			oTaskTrade.setTradeControler(this);
			oTaskTrade.starTask();
			WorkerFactory.getStockExchange().getRules().addRule(oTaskTrade);
		}
		catch(final Exception e) 
		{
			m_strCurrentState = Thread.currentThread().getName() +  " Thread exception : " + CommonUtils.getExceptionMessage(e);
			System.err.printf(m_strCurrentState + "\r\n");
		}
	}
	
	protected void changeReverseRateTrade(final List<ITradeTask> aTaskTrades)
	{
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(getRateInfo());
		final CandlestickType oCandlestickType = oCandlestick.getType();
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(getRateInfo());
		BigDecimal nOrderSellVolume = BigDecimal.ZERO; 

		boolean bIsNeedReverse = false;
		if (oCandlestickType.equals(CandlestickType.WHITE_AND_TWO_BLACK) || oCandlestickType.equals(CandlestickType.THREE_BLACK))
		{
			final Date oMaxDateCreate = DateUtils.addMinutes(new Date(), -5); 
			for(final ITradeTask oTaskTrade : aTaskTrades)
			{
				final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
				if (!OrderSide.SELL.equals(oOrder.getSide()))
					return;
				if (null == oOrder.getCreated() || oOrder.getCreated().after(oMaxDateCreate))
					continue;
			
				nOrderSellVolume = nOrderSellVolume.add(oOrder.getVolume());
			}
			bIsNeedReverse = (nOrderSellVolume.compareTo(nMinTradeVolume) > 0);
		}
		else
		if (oCandlestickType.isFall())
		{
			final Date oMaxDateCreate = DateUtils.addMinutes(new Date(), -15); 
			for(final ITradeTask oTaskTrade : aTaskTrades)
			{
				final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
				if (!OrderSide.SELL.equals(oOrder.getSide()))
					return;
				if (null == oOrder.getCreated() || oOrder.getCreated().after(oMaxDateCreate))
					continue;
			
				nOrderSellVolume = nOrderSellVolume.add(oOrder.getVolume());
			}
			bIsNeedReverse = (nOrderSellVolume.compareTo(nMinTradeVolume) > 0);
		}
		
		if (!bIsNeedReverse)
			return;		

		/*for(final ITradeTask oTaskTrade : aTaskTrades)
		{
			final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
			TradeUtils.removeOrder(oOrder, getRateInfo());
			oTaskTrade.setTradeControler(ITradeControler.NULL);
			tradeDone((TaskTrade)oTaskTrade);
			WorkerFactory.getStockExchange().getRules().removeRule(oTaskTrade);
		}
		
		m_oTradesInfo.addSell(BigDecimal.ZERO, nOrderSellVolume);
		getAllTradesInfo().put(getRateInfo(), m_oTradesInfo);
		
		getRateInfo() = RateInfo.getReverseRate(getRateInfo());
		getAllTradesInfo().get(getRateInfo()).addSell(nOrderSellVolume, BigDecimal.ZERO);
		m_oTradesInfo = getAllTradesInfo().get(getRateInfo());
		
		m_oBuyStrategy = null;*/
		
		m_strCurrentState = "Reverse trade !!! ";
	}

	public void remove()
	{
		final List<ITradeTask> aTaskTrades = getTaskTrades();
		for(final ITradeTask oTaskTrade : aTaskTrades)
			oTaskTrade.setTradeControler(ITradeControler.NULL);
	}
	
	public void tradeStart(final TaskTrade oTaskTrade) 
	{
		getTradesInfo().tradeStart(oTaskTrade);
	}

	public void buyDone(final TaskTrade oTaskTrade) 
	{
		getTradesInfo().buyDone(oTaskTrade);
	}
	
	public void tradeDone(final TaskTrade oTaskTrade)
	{
		getTradesInfo().tradeDone(oTaskTrade);

		final List<ITradeTask> aTaskTrades = getTaskTrades();
		m_oTradesInfo.updateOrderInfo(aTaskTrades);
		
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.TRADERESULT, oTaskTrade.getTradeInfo().getInfo() + "\r\n\r\n" + getType() + 
				" " + getRateInfo().toString() + "\r\n" + getTradesInfo().getInfo());
	}	

	public void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume) 
	{
		getTradesInfo().addBuy(nSpendSum, nBuyVolume);
		if (nSpendSum.compareTo(BigDecimal.ZERO) != 0 || nBuyVolume.compareTo(BigDecimal.ZERO) != 0)
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, getRateInfo().toString() +  " Buy : " + MathUtils.toCurrencyString(nSpendSum) + 
					" / " + MathUtils.toCurrencyStringEx(nBuyVolume));
	}
	
	public void addSell(final BigDecimal nReceivedSum, final BigDecimal nSoldVolume) 
	{
		getTradesInfo().addSell(nReceivedSum, nSoldVolume);
		if (nReceivedSum.compareTo(BigDecimal.ZERO) != 0 || nSoldVolume.compareTo(BigDecimal.ZERO) != 0)
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, getRateInfo().toString() +  " Sell : " + MathUtils.toCurrencyString(nReceivedSum) + 
					" / " + MathUtils.toCurrencyStringEx(nSoldVolume));
	}
	
	@Override public void setParameter(final String strParameterName, final String strValue)
	{
		if (strParameterName.equalsIgnoreCase("tradeSum"))
			m_oTradesInfo.setSum(MathUtils.fromString(strValue), m_nMaxTrades);
		
		if (strParameterName.equalsIgnoreCase("tradeCount"))
			m_nMaxTrades = Integer.decode(strValue);
		
		if (strParameterName.equalsIgnoreCase("buyStrategy"))
			m_oBuyStrategy = StrategyFactory.getBuyStrategy(strValue);
				
		super.setParameter(strParameterName, strValue);
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		return  getRateInfo() + "\r\n" + getTradesInfo().getInfo() + 
			(StringUtils.isNotBlank(m_strCurrentState) ? "\r\nState : [" + m_strCurrentState + "]" : StringUtils.EMPTY);
	}
}

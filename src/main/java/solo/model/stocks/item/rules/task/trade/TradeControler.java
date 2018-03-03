package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RulesFactory;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.item.rules.task.strategy.StrategyUtils;
import solo.model.stocks.item.rules.task.strategy.trade.ITradeStrategy;
import solo.model.stocks.source.IStockSource;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;
import solo.transport.telegram.TelegramTransport;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TradeControler extends TaskBase implements ITradeControler
{
	private static final long serialVersionUID = 2548242566461334806L;
	
	public static final String NAME = "CONTROLER";

	public static final String TRADE_SUM_PARAMETER = "tradeSum";
	public static final String TRADE_COUNT_PARAMETER = "tradeCount";
	public static final String TRADE_STRATEGY_PARAMETER = "tradeStrategy";
	
	final static public String TRADE_SUM = "#sum#";
	final static public String MAX_TARDES = "#count#";
	final static public String STRATEGY = "#strategy#";
	
	protected Integer m_nMaxTrades;
	protected Map<RateInfo, TradesInfo> m_oAllTradesInfo = new HashMap<RateInfo, TradesInfo>();
	protected TradesInfo m_oTradesInfo;
	protected ITradeStrategy m_oTradeStrategy = null;
	
	public TradeControler(String strCommandLine)
	{
		super(strCommandLine, CommonUtils.mergeParameters(TRADE_SUM, MAX_TARDES, STRATEGY));
		initTrade();
	}
	
	public TradeControler(final String strCommandLine, final String strTemplate)
	{
		super(strCommandLine, CommonUtils.mergeParameters(TRADE_SUM, MAX_TARDES, STRATEGY, strTemplate));
		initTrade();
	}

	void initTrade()
	{
		m_nMaxTrades = getParameterAsInt(MAX_TARDES, 1);
		final BigDecimal nTradeSum = getParameterAsBigDecimal(TRADE_SUM);
		getTradesInfo().setSum(nTradeSum, m_nMaxTrades);
		setDefaultTradeStrategy();
	}
	
	@Override public String getType()
	{
		return "CONTROLER [" + getRateInfo() + "]";   
	}
	
	@Override public void setID(final int nID)
	{
		super.setID(nID);
		getTradesInfo().setRuleID(nID);
	}
	
	protected IStockSource getStockSource()
	{
		return WorkerFactory.getStockSource(this);
	}
	
	public void setDefaultTradeStrategy()
	{
		setTradeStrategy(getParameterAsTradeStrategy(STRATEGY, TradeUtils.getTradeStrategy(m_oRateInfo)));
	}
	
	public void setTradeStrategy(final ITradeStrategy oTradeStrategy)
	{
		if (null == oTradeStrategy || (null != m_oTradeStrategy && m_oTradeStrategy.equals(oTradeStrategy)))
			return;
		
		m_oTradeStrategy = oTradeStrategy;
		getTradesInfo().addToHistory("Set trade strategy [" + m_oTradeStrategy.getName() + "]");
	}
	
	@Override public ControlerState getControlerState()
	{
		if (m_nMaxTrades == 0)
			return ControlerState.WAIT;
		
		if (m_nMaxTrades > 0)
			return ControlerState.WORK;
		
		final List<ITradeTask> aTaskTrades = getTaskTrades();
		return (aTaskTrades.size() > 0 ? ControlerState.STOPPING : ControlerState.STOPPED);
	}
	
	@Override public void setControlerState(ControlerState oControlerState)
	{
		if (oControlerState.equals(getControlerState()))
			return;
		
		if (ControlerState.WAIT.equals(oControlerState))
			m_nMaxTrades = 0;
		else if (ControlerState.STOPPING.equals(oControlerState) || ControlerState.STOPPED.equals(oControlerState))
			m_nMaxTrades = -1;
		else if (ControlerState.WORK.equals(oControlerState))
			m_nMaxTrades = getParameterAsInt(MAX_TARDES, 1);
		
		final String strMessage = "Set new controler state [" + getRateInfo() + "] [" + getID() + "] - " + oControlerState;
		getTradesInfo().addToHistory(strMessage);
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.TRACE, strMessage);
	} 
	
	@Override public String getInfo()
	{
		String strInfo = "[" + getRateInfo() + "]";  		
		for(final ITradeTask oTaskTrade : getTaskTrades())
		{
			final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
			if (null == oOrder)
				continue;
			
			strInfo += (null == oOrder.getSide() ? "None" : oOrder.getSide().toString()) + "/" + 
					(null == oOrder.getPrice() ? StringUtils.EMPTY : MathUtils.toCurrencyStringEx3(oOrder.getPrice()) + "/") + 
					MathUtils.toCurrencyStringEx3(oTaskTrade.getTradeInfo().getTradeSum()) + ";";  
		}
		
		return strInfo + "[" + getControlerState() + "]" + getTradesInfo().getCurrentState();   
	}
	
	public String getFullInfo()
	{ 
		String strInfo = StringUtils.EMPTY;
		
		final List<ITradeTask> aTaskTrades = getTaskTrades();
		final List<List<String>> aButtons = new LinkedList<List<String>>();
		for(final ITradeTask oTaskTrade : aTaskTrades)
		{
			final List<String> aTradeButtons = Arrays.asList(oTaskTrade.getInfo() + "=trade_" + oTaskTrade.getID());
			aButtons.add(aTradeButtons);
		}
		
		final StateAnalysisResult oStateAnalysisResult = WorkerFactory.getStockExchange().getLastAnalysisResult();
    	final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
    	strInfo += GetRateInfoCommand.getRateData(m_oRateInfo, oAnalysisResult);
 		
		return getTradesInfo().getInfo() + "[" + getTradeStrategy().getName() + "][" + m_nMaxTrades + "]\r\n\r\n" + strInfo +
				(aButtons.size() > 0 ? "BUTTONS\r\n" + TelegramTransport.getButtons(aButtons) : StringUtils.EMPTY);
	}
	
	public RateInfo getRateInfo()
	{
		return getTradesInfo().getRateInfo();
	}
	
	public ITradeStrategy getTradeStrategy()
	{
		return m_oTradeStrategy;
	}
	
	public int getMaxTrades()
	{
		return m_nMaxTrades;
	}
	
	public TradesInfo getTradesInfo()
	{
		if (null == m_oTradesInfo)
			m_oTradesInfo = new TradesInfo(m_oRateInfo, m_nID);

		return m_oTradesInfo;   
	}
	
	public void setTradesInfo(final TradesInfo oTradesInfo)
	{
		m_oTradesInfo = oTradesInfo;   
	}
	
	public Map<RateInfo, TradesInfo> getAllTradesInfo()
	{
		if (null == m_oAllTradesInfo || m_oAllTradesInfo.size() == 0)
		{
			m_oAllTradesInfo = new HashMap<RateInfo, TradesInfo>();
			m_oAllTradesInfo.put(getRateInfo(), new TradesInfo(getRateInfo(), m_nID));
			
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(getRateInfo());
			m_oAllTradesInfo.put(oReverseRateInfo, new TradesInfo(oReverseRateInfo, m_nID));
		}
		return m_oAllTradesInfo;   
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
	
	@Override public void check(final StateAnalysisResult oStateAnalysisResult)
	{
		getTradesInfo().setCurrentState(StringUtils.EMPTY);
		
		List<ITradeTask> aTaskTrades = getTaskTrades();
		getTradeStrategy().checkTrades(aTaskTrades, this);
		
		final List<ITradeTask> aListRemoveTrades = new LinkedList<ITradeTask>();
		for(final ITradeTask oTaskTrade : aTaskTrades)
		{
			final boolean bIsRemoveTrade = getTradeStrategy().checkTrade(oTaskTrade, aTaskTrades, this);
			if (bIsRemoveTrade)
				aListRemoveTrades.add(oTaskTrade);
		}
		
		for(final ITradeTask oTaskTrade : aListRemoveTrades)
		{
			aTaskTrades.remove(oTaskTrade);
			oTaskTrade.setTradeControler(ITradeControler.NULL);
			WorkerFactory.getStockExchange().getRules().removeRule(oTaskTrade);
		}

		for(final ITradeTask oTaskTrade : aTaskTrades)
			oTaskTrade.check(oStateAnalysisResult);
		
		aTaskTrades = getTaskTrades();
		if (getTradeStrategy().isCreateNewTrade(aTaskTrades, this))
			createNewTrade(oStateAnalysisResult, aTaskTrades);	
	}
	
	protected void createNewTrade(final StateAnalysisResult oStateAnalysisResult, List<ITradeTask> aTaskTrades)
	{
		try
		{
			final BigDecimal oMinTradeSum = TradeUtils.getMinTradeSum(getRateInfo()); 
			final BigDecimal nTotalSum = m_oTradesInfo.getSum().add(m_oTradesInfo.getSumToSell());
			if (nTotalSum.compareTo(oMinTradeSum) < 0)
			{
				getTradesInfo().setCurrentState("Wait buy. No money - " + MathUtils.toCurrencyStringEx2(nTotalSum) + " < " + MathUtils.toCurrencyStringEx2(oMinTradeSum));
				return;
			}
				
			BigDecimal nBuySum = MathUtils.getRoundedBigDecimal(nTotalSum.doubleValue() / m_nMaxTrades, TradeUtils.getVolumePrecision(getRateInfo()));
			final CurrencyAmount oCurrencyAmount = getStockSource().getUserInfo(getRateInfo()).getMoney().get(getRateInfo().getCurrencyTo());
			if (null != oCurrencyAmount && oCurrencyAmount.getBalance().compareTo(nBuySum) < 0)
				nBuySum = oCurrencyAmount.getBalance();

			if (nBuySum.compareTo(oMinTradeSum) < 0)
			{
				nBuySum = oMinTradeSum;
				if (null != oCurrencyAmount && oCurrencyAmount.getBalance().compareTo(nBuySum) < 0)
				{
					getTradesInfo().setCurrentState("Wait buy. No money on balance - " + MathUtils.toCurrencyStringEx2(nBuySum) + " < " + MathUtils.toCurrencyStringEx2(oCurrencyAmount.getBalance()));
					return;
				}
			}
			
			final BigDecimal nNeedSellVolume = m_oTradesInfo.getFreeVolume();
			BigDecimal nNeedSellVolumeSum = BigDecimal.ZERO;
			if (nNeedSellVolume.compareTo(BigDecimal.ZERO) > 0)
			{
				final RateAnalysisResult oRateAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
				final BigDecimal nBuyPrice = StrategyUtils.getBestPrice(oRateAnalysisResult.getBidsOrders());
				nNeedSellVolumeSum = nBuyPrice.multiply(nNeedSellVolume);
				nBuySum = nBuySum.add(nNeedSellVolumeSum);
			}
			
			final String strRuleInfo = getTraderName() + "_" + getRateInfo() + "_" + nBuySum;
			final IRule oRule = RulesFactory.getRule(strRuleInfo);
			final TaskTrade oTaskTrade = ((TaskTrade)oRule);
			
			if (nNeedSellVolume.compareTo(BigDecimal.ZERO) > 0)
				oTaskTrade.getTradeInfo().addBuy(nNeedSellVolumeSum, nNeedSellVolume);
			
			getTradeStrategy().startNewTrade(oTaskTrade, this);
			oTaskTrade.setTradeControler(this);
			oTaskTrade.starTask();
			oTaskTrade.check(oStateAnalysisResult);
			WorkerFactory.getStockExchange().getRules().addRule(oTaskTrade);
		}
		catch(final Exception e) 
		{
			getTradesInfo().setCurrentState(Thread.currentThread().getName() +  " Thread exception : " + CommonUtils.getExceptionMessage(e));
			System.err.printf(getTradesInfo().getCurrentState() + "\r\n");
		}
	}

	protected String getTraderName()
	{
		return TaskTrade.NAME;
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
		WorkerFactory.getStockExchange().getManager().tradeStart(oTaskTrade);
	}

	public void buyDone(final TaskTrade oTaskTrade) 
	{
		getTradesInfo().buyDone(oTaskTrade);
		WorkerFactory.getStockExchange().getManager().buyDone(oTaskTrade);
	}
	
	public void tradeDone(final TaskTrade oTaskTrade)
	{
		getTradesInfo().tradeDone(oTaskTrade);

		final List<ITradeTask> aTaskTrades = getTaskTrades();
		m_oTradesInfo.updateOrderInfo(aTaskTrades);
		WorkerFactory.getStockExchange().getManager().tradeDone(oTaskTrade);
		
		if (ManagerUtils.isHasRealControlers(getRateInfo()))
		{
			final MessageLevel oMessageLevel = (!ManagerUtils.isTestObject(this) ? MessageLevel.TRADERESULT : MessageLevel.TESTTRADERESULT);
			final String strMessage = (StringUtils.isNotBlank(oTaskTrade.getTradeInfo().getInfo()) ?  oTaskTrade.getTradeInfo().getInfo() + "\r\n\r\n" : StringUtils.EMPTY) + 
										getTradesInfo().getInfo();
			WorkerFactory.getMainWorker().sendMessage(oMessageLevel,strMessage);
		}
	}	

	public void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume) 
	{
		getTradesInfo().addBuy(nSpendSum, nBuyVolume);
		if (nSpendSum.compareTo(BigDecimal.ZERO) != 0 || nBuyVolume.compareTo(BigDecimal.ZERO) != 0)
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, getRateInfo().toString() +  " Buy : " + MathUtils.toCurrencyStringEx2(nSpendSum) + 
					" / " + MathUtils.toCurrencyStringEx2(nBuyVolume));
		WorkerFactory.getStockExchange().getManager().addBuy(nSpendSum, nBuyVolume);
	}
	
	public void addSell(final BigDecimal nReceivedSum, final BigDecimal nSoldVolume) 
	{
		getTradesInfo().addSell(nReceivedSum, nSoldVolume);
		if (nReceivedSum.compareTo(BigDecimal.ZERO) != 0 || nSoldVolume.compareTo(BigDecimal.ZERO) != 0)
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, getRateInfo().toString() +  " Sell : " + MathUtils.toCurrencyStringEx2(nReceivedSum) + 
					" / " + MathUtils.toCurrencyStringEx2(nSoldVolume));
		WorkerFactory.getStockExchange().getManager().addSell(nReceivedSum, nSoldVolume);
	}
	
	@Override public String getParameter(final String strParameterName)
	{
		if (strParameterName.equalsIgnoreCase(TRADE_COUNT_PARAMETER))
			return m_nMaxTrades.toString();
		
		if (strParameterName.equalsIgnoreCase(TRADE_SUM_PARAMETER))
			getTradesInfo().getSum().toString();	
		
		if (strParameterName.equalsIgnoreCase(TRADE_STRATEGY_PARAMETER))
			return m_oTradeStrategy.getName();
		
		if (strParameterName.equalsIgnoreCase(TradeControler.MAX_TARDES))
			return StringUtils.isBlank(super.getParameter(MAX_TARDES)) ? "1" : super.getParameter(MAX_TARDES);

		return super.getParameter(strParameterName);
	}
	
	@Override public void setParameter(final String strParameterName, final String strValue)
	{		
		if (strParameterName.equalsIgnoreCase(TRADE_COUNT_PARAMETER))
			m_nMaxTrades = Integer.decode(strValue);
	
		if (strParameterName.equalsIgnoreCase(TRADE_SUM_PARAMETER))
			getTradesInfo().setSum(new BigDecimal(Integer.decode(strValue)), m_nMaxTrades);	
		
		if (strParameterName.equalsIgnoreCase(TRADE_STRATEGY_PARAMETER))
		{
			if (strValue.equalsIgnoreCase("default"))
				setDefaultTradeStrategy();
			else
				setTradeStrategy(StrategyFactory.getTradeStrategy(strValue));
		}
		
		if (strParameterName.equalsIgnoreCase("addNeedSellVolume"))
		{
			final BigDecimal nNeedSellVolume = new BigDecimal(Double.parseDouble(strValue));
			getTradesInfo().addBuy(BigDecimal.ZERO, nNeedSellVolume);
		}
				
		super.setParameter(strParameterName, strValue);
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		return getTradesInfo().getInfo();
	}
}

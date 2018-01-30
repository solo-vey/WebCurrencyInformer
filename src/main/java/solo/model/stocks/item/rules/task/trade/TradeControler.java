package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RulesFactory;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.command.system.GetRateChartCommand;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.trade.GetTradeInfoCommand;
import solo.model.stocks.item.command.trade.SetTaskParameterCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.item.rules.task.strategy.trade.DropSellTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.ITradeStrategy;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TradeControler extends TaskBase implements ITradeControler
{
	private static final long serialVersionUID = 2548242566461334806L;
	
	final static public String TRADE_SUM = "#sum#";
	final static public String MAX_TARDES = "#count#";
	final static public String STRATEGY = "#strategy#";
	final static public ITradeStrategy DEFAULT_TRADE_STRATEGY = StrategyFactory.getTradeStrategy(DropSellTradeStrategy.NAME);
	
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
		m_oTradeStrategy = getParameterAsTradeStrategy(STRATEGY, DEFAULT_TRADE_STRATEGY);
	}
	
	@Override public String getType()
	{
		return "CONTROLER [" + getRateInfo() + "]";   
	}
	
	public String getInfo()
	{
		String strInfo = getType() + 
				" " + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, m_nID, GetTradeInfoCommand.FULL_PARAMETER, "true") + 
				"\r\n";  
		
		for(final ITradeTask oTaskTrade : getTaskTrades())
		{
			final String strOrderSide = (null == oTaskTrade.getTradeInfo().getOrder().getSide() ? "None" : oTaskTrade.getTradeInfo().getOrder().getSide().toString());
			strInfo += strOrderSide + ";";  
		}
		strInfo += "[" + m_nMaxTrades + "] ";
		
		return strInfo + getTradesInfo().getCurrentState() + 
				" " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, m_nID);   
	}
	
	public String getFullInfo()
	{ 
		String strInfo = getRateInfo().toString().toUpperCase();
		
		final List<ITradeTask> aTaskTrades = getTaskTrades();
		for(final ITradeTask oTaskTrade : aTaskTrades)
			strInfo += " -> " + oTaskTrade.getInfo() + "\r\n"; 
		
		final StateAnalysisResult oStateAnalysisResult = WorkerFactory.getStockExchange().getLastAnalysisResult();
    	final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
    	strInfo += "\r\n" + GetRateInfoCommand.getRateData(m_oRateInfo, oAnalysisResult);
		
		strInfo += "\r\nStrategy [" + getTradeStrategy().getName() + "]";
		strInfo += "; tradeCount [" + m_nMaxTrades + "]\r\n";
		return getTradesInfo().getInfo() + "\r\n" + strInfo + 
				CommandFactory.makeCommandLine(SetTaskParameterCommand.class, SetTaskParameterCommand.RULE_ID_PARAMETER, m_nID, 
							SetTaskParameterCommand.NAME_PARAMETER, "tradeCount", SetTaskParameterCommand.VALUE_PARAMETER, "0") + "\r\n" +
				CommandFactory.makeCommandLine(GetRateChartCommand.class, GetRateInfoCommand.RATE_PARAMETER, getRateInfo());
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
		
		final List<ITradeTask> aTaskTrades = getTaskTrades();
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
			final CurrencyAmount oCurrencyAmount = WorkerFactory.getStockSource().getUserInfo(getRateInfo()).getMoney().get(getRateInfo().getCurrencyTo());
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
			
			final String strRuleInfo = "trade" + "_" + getRateInfo() + "_" + nBuySum;
			final IRule oRule = RulesFactory.getRule(strRuleInfo);
			final TaskTrade oTaskTrade = ((TaskTrade)oRule);
			
			if (m_oTradesInfo.getFreeVolume().compareTo(BigDecimal.ZERO) > 0)
			{
				final BigDecimal nNeedSellVolume = m_oTradesInfo.getFreeVolume();
				oTaskTrade.getTradeInfo().addBuy(BigDecimal.ZERO, nNeedSellVolume);
			}
			oTaskTrade.getTradeInfo().setPriviousLossSum(getTradesInfo().getLossSum());
			
			getTradeStrategy().startNewTrade(oTaskTrade, this);
			oTaskTrade.setTradeControler(this);
			oTaskTrade.starTask();
			WorkerFactory.getStockExchange().getRules().addRule(oTaskTrade);
		}
		catch(final Exception e) 
		{
			getTradesInfo().setCurrentState(Thread.currentThread().getName() +  " Thread exception : " + CommonUtils.getExceptionMessage(e));
			System.err.printf(getTradesInfo().getCurrentState() + "\r\n");
		}
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
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, getRateInfo().toString() +  " Buy : " + MathUtils.toCurrencyStringEx2(nSpendSum) + 
					" / " + MathUtils.toCurrencyStringEx2(nBuyVolume));
	}
	
	public void addSell(final BigDecimal nReceivedSum, final BigDecimal nSoldVolume) 
	{
		getTradesInfo().addSell(nReceivedSum, nSoldVolume);
		if (nReceivedSum.compareTo(BigDecimal.ZERO) != 0 || nSoldVolume.compareTo(BigDecimal.ZERO) != 0)
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, getRateInfo().toString() +  " Sell : " + MathUtils.toCurrencyStringEx2(nReceivedSum) + 
					" / " + MathUtils.toCurrencyStringEx2(nSoldVolume));
	}
	
	@Override public void setParameter(final String strParameterName, final String strValue)
	{		
		if (strParameterName.equalsIgnoreCase("tradeCount"))
			m_nMaxTrades = Integer.decode(strValue);
	
		if (strParameterName.equalsIgnoreCase("tradeSum"))
			getTradesInfo().setSum(new BigDecimal(Integer.decode(strValue)), m_nMaxTrades);	
		
		if (strParameterName.equalsIgnoreCase("tradeStrategy"))
		{
			final ITradeStrategy oTradeStrategy = StrategyFactory.getTradeStrategy(strValue);
			if (null != oTradeStrategy)
				m_oTradeStrategy = oTradeStrategy;
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
		return getRateInfo() + "\r\n" + getTradesInfo().getInfo();
	}
}

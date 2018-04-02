package solo.model.stocks.item.rules.task.manager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.command.system.AddRateCommand;
import solo.model.stocks.item.rules.task.money.Money;
import solo.model.stocks.item.rules.task.strategy.manager.BaseManagerStrategy;
import solo.model.stocks.item.rules.task.strategy.manager.IManagerStrategy;
import solo.model.stocks.item.rules.task.trade.ControlerState;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;
import solo.utils.MathUtils;
import solo.utils.ResourceUtils;

public class StockManager implements IStockManager
{
	public static final String OPERATIONS_ALL = "all";
	public static final String OPERATIONS_NONE = StringUtils.EMPTY;
	public static final String OPERATION_TRACK_TRADES = ";trackTrades;";
	public static final String OPERATION_CHECK_RATES = ";checkRates;";
	public static final String OPERATIONS_DEFAULT = OPERATIONS_ALL;
	
	final protected StockManagesInfo m_oStockManagesInfo;
	final protected Money m_oMoney;
	protected String m_strOperations = OPERATIONS_NONE;
	final protected IManagerStrategy m_oManagerStrategy;
	final protected ManagerHistory m_oManagerHistory;
	
	public StockManager(final IStockExchange oStockExchange)
	{
		m_oStockManagesInfo = StockManagesInfo.load(oStockExchange);
		m_oMoney = Money.load(oStockExchange);
		m_oManagerHistory = new ManagerHistory(oStockExchange);
		m_oManagerStrategy = new BaseManagerStrategy(oStockExchange);
		setOperations(OPERATIONS_DEFAULT);
	}
	
	protected IManagerStrategy getManagerStrategy()
	{
		return m_oManagerStrategy;
	}	
	
	public void manage(final StateAnalysisResult oStateAnalysisResult) 
	{		
		startTestControlers();
		removeStoppedControlers();
		
		if (!getIsOperationAvalible(OPERATIONS_ALL))
			return;
		
		final Map<BigDecimal, RateInfo> oProfitabilityRates = getManagerStrategy().getProfitabilityRates();
		final Map<BigDecimal, RateInfo> oUnProfitabilityRates = getManagerStrategy().getUnProfitabilityRates();

		lookForProspectiveRate();
		checkUnprofitability(oUnProfitabilityRates);
		removeHandUpTrades(oProfitabilityRates, oUnProfitabilityRates);
		checkProfitableRates(oProfitabilityRates);
	}
	
	@Override public Money getMoney()
	{
		return m_oMoney;
	}	
	
	protected void removeStoppedControlers()
	{
		final Map<Integer, IRule> oRules = WorkerFactory.getStockExchange().getRules().getRules();
		final List<IRule> oStoppedRules = new LinkedList<IRule>();
		for(final IRule oRule : oRules.values())
		{
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
			if (null != oControler && oControler.getControlerState().isStopped())
				oStoppedRules.add(oRule);
		}
		
		for(final IRule oRule : oStoppedRules)
		{
			WorkerFactory.getStockExchange().getRules().removeRule(oRule);
			addToHistory("Remove stopped controler [" + oRule.getRateInfo() + "] [" + oRule.getID() + "]", MessageLevel.DEBUG);
		}
	}
	
	protected void removeHandUpTrades(final Map<BigDecimal, RateInfo> oProfitabilityRates, final Map<BigDecimal, RateInfo> oUnProfitabilityRates)
	{
		final Map<Integer, IRule> oRules = WorkerFactory.getStockExchange().getRules().getRules();
		final List<ITradeTask> oRemoveTrades = new LinkedList<ITradeTask>();
		for(final IRule oRule : oRules.values())
		{
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRule.getRateInfo());
			if (!oProfitabilityRates.containsValue(oReverseRateInfo))
				continue;
			
			final ITradeTask oTaskTrade = TradeUtils.getRuleAsTradeTask(oRule);
			if (null == oTaskTrade)
				continue;
			
			final ITradeControler oControler = oTaskTrade.getTradeControler();
			if (null == oControler || !oControler.getControlerState().isStop())
				continue;
			
			final int nRemoveHungUpOrderHours = ResourceUtils.getIntFromResource("stock.remove.hung_up_order.hours", WorkerFactory.getStockExchange().getStockProperties(), 3);
			final Date oMinDateCreate = DateUtils.addHours(new Date(), -nRemoveHungUpOrderHours);
			final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
			if (null != oOrder.getCreated() && oOrder.getCreated().before(oMinDateCreate))
				oRemoveTrades.add(oTaskTrade);
		}
		
		for(final ITradeTask oTaskTrade : oRemoveTrades)
		{
			WorkerFactory.getStockExchange().getRules().removeRule(oTaskTrade);
			addToHistory("Remove hung up trade [" + oTaskTrade.getRateInfo() + "] [" + oTaskTrade.getID() + "]", MessageLevel.TRADERESULTDEBUG);
		}		
	}
	
	protected boolean startStoppingControlers(final RateInfo oRateInfo, final BigDecimal oRateProfitabilityPrcent)
	{
		boolean bIsHadStoppingControlers = false;
		final List<Entry<Integer, IRule>> oRules = WorkerFactory.getStockExchange().getRules().getRules(oRateInfo);
		for(final Entry<Integer, IRule> oRuleInfo : oRules)
		{
			final IRule oRule = oRuleInfo.getValue();
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
			if (null == oControler)
				continue;
			
			if (oControler.getControlerState().isStop())
			{
				oControler.setControlerState(ControlerState.WORK);
				addToHistory("Goto to WORK STOPPING controler [" + oRule.getRateInfo() + "] [" + oRule.getID() + "]. Good profit [" + oRateProfitabilityPrcent + "%]", MessageLevel.DEBUG);
				bIsHadStoppingControlers = true;
			}
		}
		
		return bIsHadStoppingControlers;
	}
	
	protected void checkProfitableRates(final Map<BigDecimal, RateInfo> oProfitabilityRates)
	{
		if (!getIsOperationAvalible(OPERATION_CHECK_RATES))
			return;
		
		final List<Entry<BigDecimal, RateInfo>> oCreateControlers = new LinkedList<Entry<BigDecimal, RateInfo>>();
		for(final Entry<BigDecimal, RateInfo> oRateProfitabilityInfo : oProfitabilityRates.entrySet())
		{
			final RateInfo oRateInfo = oRateProfitabilityInfo.getValue();
			if (ManagerUtils.isHasRealWorkingControlers(oRateInfo))
				continue;
			
			if (startStoppingControlers(oRateInfo, oRateProfitabilityInfo.getKey()))
				continue;
			
			oCreateControlers.add(oRateProfitabilityInfo);
		}
		
		if (oCreateControlers.size() == 0)
			return;
		
		for(final Entry<BigDecimal, RateInfo> oRateProfitabilityInfo : oCreateControlers)
		{
			final RateInfo oRateInfo = oRateProfitabilityInfo.getValue();
			final BigDecimal nSum = TradeUtils.getMinTradeSum(oRateInfo).multiply(new BigDecimal(2.2));	
			if (ManagerUtils.createTradeControler(oRateInfo, nSum).isEmpty())
				addToHistory("Create controler [" + oRateInfo + "]. Good profit [" + oRateProfitabilityInfo.getKey() + "%]", MessageLevel.TRADERESULTDEBUG);
		}
	}
	
	protected void checkUnprofitability(final Map<BigDecimal, RateInfo> oUnProfitabilityRates)
	{
		if (!getIsOperationAvalible(OPERATION_CHECK_RATES))
			return;
			
		for(final Entry<BigDecimal, RateInfo> oRateProfitabilityInfo : oUnProfitabilityRates.entrySet())
		{
			final RateInfo oRateInfo = oRateProfitabilityInfo.getValue();			
			final List<Entry<Integer, IRule>> oRules = WorkerFactory.getStockExchange().getRules().getRules(oRateInfo);
			for(final Entry<Integer, IRule> oRuleInfo : oRules)
			{
				final IRule oRule = oRuleInfo.getValue();
				final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
				if (null == oControler || ManagerUtils.isTestObject(oControler))
					continue;
				
				if (oControler.getControlerState().isStopping())
					removeBuyInStoppingControler(oRateInfo, oControler);		
				
				if (oControler.getControlerState().isStop())
					continue;
				
				oControler.setControlerState(ControlerState.STOPPING);
				addToHistory("Stopping controler [" + oRule.getRateInfo() + "] [" + oRule.getID() + "]. Bad profit [" + oRateProfitabilityInfo.getKey() + "]", MessageLevel.TRADERESULTDEBUG);
			}
		}
	}

	protected void removeBuyInStoppingControler(final RateInfo oRateInfo, final ITradeControler oControler)
	{
		final List<ITradeTask> oRemoveTrades = new LinkedList<ITradeTask>();
		for(final IRule oRule : WorkerFactory.getStockExchange().getRules().getRules().values())
		{
			final ITradeTask oTaskTrade = TradeUtils.getRuleAsTradeTask(oRule);
			if (null == oTaskTrade)
				continue;
			
			if (null == oTaskTrade.getTradeControler() || !oTaskTrade.getTradeControler().equals(oControler))
				continue;
			
			if (OrderSide.BUY.equals(oTaskTrade.getTradeInfo().getTaskSide()) &&
					TradeUtils.isVerySmallVolume(oRateInfo, oTaskTrade.getTradeInfo().getBoughtVolume()))
				oRemoveTrades.add(oTaskTrade);
		}
		
		for(final ITradeTask oTaskTrade : oRemoveTrades)
		{
			WorkerFactory.getStockExchange().getRules().removeRule(oTaskTrade);
			addToHistory("Remove BUY trade [" + oTaskTrade.getRateInfo() + "] [" + oTaskTrade.getID() + "] in STOPPING controler", MessageLevel.TRADERESULTDEBUG);
		}
	}

	protected void startTestControlers()
	{
		for(final RateInfo oRateInfo : WorkerFactory.getStockSource().getRates())
		{
			if (!ManagerUtils.isHasTestControlers(oRateInfo))
			{
				ManagerUtils.createTestControler(oRateInfo);
				addToHistory("Start test controler [" + oRateInfo + "]", MessageLevel.TESTTRADERESULT);
			}
			
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo);
			if (!ManagerUtils.isHasTestControlers(oReverseRateInfo))
			{
				ManagerUtils.createTestControler(oReverseRateInfo);
				addToHistory("Start test reverse controler [" + oReverseRateInfo + "]", MessageLevel.TESTTRADERESULT);
			}	
		}
	}
	
	private void lookForProspectiveRate()
	{
		try
		{
			final Map<RateInfo, RateStateShort> oAllRateState = WorkerFactory.getStockSource().getAllRateState();
			final List<RateInfo> oProspectiveRates = ManagerUtils.getProspectiveRates(oAllRateState, BigDecimal.ZERO);
			for(final RateInfo oRateInfo : oProspectiveRates)
			{
				WorkerFactory.getMainWorker().addCommand(new AddRateCommand(oRateInfo.toString()));
				addToHistory("Start prospective rate [" + oRateInfo + "]", MessageLevel.TRADERESULTDEBUG);
			}
		}
		catch(final Exception e)
		{
			WorkerFactory.onException("Can't create prospective rates list.", e);
		}
	}

	void trackTrades(final TaskTrade oTaskTrade)
	{
		if (!getIsOperationAvalible(OPERATION_TRACK_TRADES))
			return;
		
		final RateInfo oRateInfo = oTaskTrade.getTradeInfo().getRateInfo();
		final BigDecimal nTradeDelta = oTaskTrade.getTradeInfo().getDelta();
		final BigDecimal nMargin = TradeUtils.getMarginValue(oTaskTrade.getTradeInfo().getReceivedSum(), oRateInfo);
		final BigDecimal nHalfMargin = MathUtils.getBigDecimal(nMargin.doubleValue()/ 2, TradeUtils.getPricePrecision(oRateInfo));
		if (nTradeDelta.compareTo(nHalfMargin) < 0)
			stopAllControlers(oTaskTrade.getTradeInfo().getRateInfo());
		else
			startAllControlers(oTaskTrade.getTradeInfo().getRateInfo());
	}
		
	private void startAllControlers(final RateInfo oRateInfo)
	{
		final List<Entry<Integer, IRule>> oRules = WorkerFactory.getStockExchange().getRules().getRules(oRateInfo);
		for(final Entry<Integer, IRule> oRuleInfo : oRules)
		{
			final IRule oRule = oRuleInfo.getValue();
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
			if (null == oControler || ManagerUtils.isTestObject(oControler))
				continue;

			if (oControler.getControlerState().isWait())
			{
				oControler.setControlerState(ControlerState.WORK);
				addToHistory("Goto WORK waiting controler [" + oRule.getRateInfo() + "] [" + oRule.getID() + "]", MessageLevel.DEBUG);
			}
		}
	}
	
	private void stopAllControlers(final RateInfo oRateInfo)
	{
		final List<Entry<Integer, IRule>> oRules = WorkerFactory.getStockExchange().getRules().getRules(oRateInfo);
		for(final Entry<Integer, IRule> oRuleInfo : oRules)
		{
			final IRule oRule = oRuleInfo.getValue();
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
			if (null == oControler || ManagerUtils.isTestObject(oControler))
				continue;
			
			if (oControler.getControlerState().isWork())
			{
				oControler.setControlerState(ControlerState.WAIT);
				addToHistory("Goto WAIT controler [" + oRateInfo + "] [" + oControler.getTradesInfo().getRuleID() + "]", MessageLevel.DEBUG);
			}
		}
	}
	
	@Override public String getOperations() 
	{ 
		return m_strOperations; 
	}
	
	@Override public void setOperations(final String strOperations) 
	{
		m_strOperations = strOperations;
	}
	
	protected boolean getIsOperationAvalible(final String strOperaion)
	{
		return (m_strOperations.equals(OPERATIONS_ALL) || m_strOperations.toLowerCase().contains(strOperaion.toLowerCase()));
	}

	@Override public StockManagesInfo getInfo()
	{
		return m_oStockManagesInfo;
	}
	
	@Override public void tradeStart(final TaskTrade oTaskTrade) 
	{
		m_oStockManagesInfo.tradeStart(oTaskTrade);
		StockManagesInfo.save(m_oStockManagesInfo);
	}
	
	@Override public void tradeDone(final TaskTrade oTaskTrade) 
	{
		trackTrades(oTaskTrade);
		m_oStockManagesInfo.tradeDone(oTaskTrade);
		StockManagesInfo.save(m_oStockManagesInfo);
	}

	@Override public void buyDone(final TaskTrade oTaskTrade) 
	{
		m_oStockManagesInfo.buyDone(oTaskTrade);
		StockManagesInfo.save(m_oStockManagesInfo);
	}
	
	@Override public void addBuy(final TaskTrade oTaskTrade, final BigDecimal nSpendSum, final BigDecimal nBuyVolume) 
	{
		if (null == oTaskTrade)
			return;
		
		m_oMoney.addBuy(oTaskTrade.getTradeControler(), nSpendSum, nBuyVolume);
	}
	
	@Override public void addSell(final TaskTrade oTaskTrade, final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) 
	{
		if (null == oTaskTrade)
			return;
		
		m_oMoney.addSell(oTaskTrade.getTradeControler(), nReceiveSum, nSoldVolume);
	} 
	
	@Override public ManagerHistory getHistory()
	{
		return m_oManagerHistory;
	}
	
	protected void addToHistory(final String strMessage, final MessageLevel oMessageLevel)
	{
		m_oManagerHistory.addMessage(strMessage);
		WorkerFactory.getMainWorker().sendMessage(oMessageLevel, strMessage);
	}
}

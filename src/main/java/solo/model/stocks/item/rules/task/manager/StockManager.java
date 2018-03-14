package solo.model.stocks.item.rules.task.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import solo.CurrencyInformer;
import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.command.system.AddRateCommand;
import solo.model.stocks.item.rules.task.strategy.manager.BaseManagerStrategy;
import solo.model.stocks.item.rules.task.strategy.manager.IManagerStrategy;
import solo.model.stocks.item.rules.task.trade.ControlerState;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
import solo.utils.ResourceUtils;

public class StockManager implements IStockManager
{
	private static final String OPERATIONS_ALL = "all";
	private static final String OPERATION_TRACK_TRADES = "trackTrades";
	private static final String OPERATION_CHECK_RATES = "checkRates";
	
	final protected StockManagesInfo m_oStockManagesInfo;
	final protected String m_strOperations = OPERATIONS_ALL;
	final protected IManagerStrategy m_oManagerStrategy;
	final protected ManagerHistory m_oManagerHistory;
	
	public StockManager(final IStockExchange oStockExchange)
	{
		m_oStockManagesInfo = load(oStockExchange);
		m_oManagerHistory = new ManagerHistory(oStockExchange);
		m_oManagerStrategy = new BaseManagerStrategy(oStockExchange);
	}
	
	protected IManagerStrategy getManagerStrategy()
	{
		return m_oManagerStrategy;
	}	
	
	public void manage(final StateAnalysisResult oStateAnalysisResult) 
	{		
		startTestControlers();
		lookForProspectiveRate();
		checkUnprofitability();
//		removeStoppedControlers();
		checkProfitableRates();
	}

	protected Map<Currency, CurrencyAmount> getMoney()
	{
		try
		{
			final StockUserInfo oUserInfo = WorkerFactory.getStockSource().getUserInfo(null);
			final Rules oRules = WorkerFactory.getStockExchange().getRules();
			return ManagerUtils.calculateStockMoney(oUserInfo, oRules);
		}
		catch(final Exception e)
		{
			return null;
		}
	}	

	protected void checkUnprofitability()
	{
		if (!getIsOperationAvalible(OPERATION_CHECK_RATES))
			return;
			
		final Map<BigDecimal, RateInfo> oUnProfitabilityRates = getManagerStrategy().getUnProfitabilityRates();
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
				
				oControler.setControlerState(ControlerState.STOPPING);
				addToHistory("Stop controler [" + oRule.getRateInfo() + "] [" + oRule.getID() + "]. Bad profit [" + oRateProfitabilityInfo.getKey() + "]");
			}
		}
	}
	
	protected void removeStoppedControlers()
	{
		final Map<Integer, IRule> oRules = WorkerFactory.getStockExchange().getRules().getRules();
		final List<IRule> oStoppedoRules = new LinkedList<IRule>();
		for(final IRule oRule : oRules.values())
		{
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
			if (null != oControler && ControlerState.STOPPED.equals(oControler.getControlerState()))
				oStoppedoRules.add(oRule);
		}
		
		for(final IRule oRule : oStoppedoRules)
		{
			WorkerFactory.getStockExchange().getRules().removeRule(oRule);
			addToHistory("Remove stopped controler [" + oRule.getRateInfo() + "] [" + oRule.getID() + "]");
		}
	}
	
	protected void checkProfitableRates()
	{
		if (!getIsOperationAvalible(OPERATION_CHECK_RATES))
			return;
		
		final Map<BigDecimal, RateInfo> oMoreProfitabilityRates = getManagerStrategy().getMoreProfitabilityRates();
		if (oMoreProfitabilityRates.size()== 0)
			return;
		
		for(final Entry<BigDecimal, RateInfo> oRateProfitabilityInfo : oMoreProfitabilityRates.entrySet())
		{
			final RateInfo oRateInfo = oRateProfitabilityInfo.getValue();
			if (ManagerUtils.isHasRealWorkingControlers(oRateInfo))
				continue;
			
			//ManagerUtils.createTradeControler(oRateInfo);
			
			final List<Entry<Integer, IRule>> oRules = WorkerFactory.getStockExchange().getRules().getRules(oRateInfo);
			for(final Entry<Integer, IRule> oRuleInfo : oRules)
			{
				final IRule oRule = oRuleInfo.getValue();
				final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
				if (null == oControler)
					continue;
				
				if (ControlerState.STOPPED.equals(oControler.getControlerState()) || ControlerState.STOPPING.equals(oControler.getControlerState()))
				{
					oControler.setControlerState(ControlerState.WORK);
					addToHistory("Start controler [" + oRule.getRateInfo() + "] [" + oRule.getID() + "]. Good profit [" + oRateProfitabilityInfo.getKey() + "%]");
				}
			}
		}
		
		final Map<Currency, CurrencyAmount> oMoney = getMoney();
		for(final Entry<BigDecimal, RateInfo> oRateProfitabilityInfo : oMoreProfitabilityRates.entrySet())
		{
			final RateInfo oRateInfo = oRateProfitabilityInfo.getValue();
			if (ManagerUtils.isHasRealWorkingControlers(oRateInfo))
				continue;

			final Currency oCurrencyTo = oRateInfo.getCurrencyTo();
			final BigDecimal nSum = TradeUtils.getMinTradeSum(oRateInfo).multiply(new BigDecimal(2.2));	
			final BigDecimal nFreeSum = (oMoney.containsKey(oCurrencyTo) ? oMoney.get(oCurrencyTo).getBalance() : BigDecimal.ZERO);
			if (nSum.compareTo(nFreeSum) > 0)
				continue;
			
			final BigDecimal nLocked = (oMoney.containsKey(oCurrencyTo) ? oMoney.get(oCurrencyTo).getLocked() : BigDecimal.ZERO);
			oMoney.put(oCurrencyTo, new CurrencyAmount(nFreeSum.add(nSum.negate()), nLocked.add(nSum)));
		}
	}

	protected void startTestControlers()
	{
		for(final RateInfo oRateInfo : WorkerFactory.getStockSource().getRates())
		{
			if (!ManagerUtils.isHasTestControlers(oRateInfo))
			{
				ManagerUtils.createTestControler(oRateInfo);
				addToHistory("Start test controler [" + oRateInfo + "]");
			}
			
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo);
			if (!ManagerUtils.isHasTestControlers(oReverseRateInfo))
			{
				ManagerUtils.createTestControler(oReverseRateInfo);
				addToHistory("Start test reverse controler [" + oReverseRateInfo + "]");
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
				addToHistory("Start prospective rate [" + oRateInfo + "]");
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

			if (!ControlerState.WAIT.equals(oControler.getControlerState()))
				continue;
			
			oControler.setControlerState(ControlerState.WORK);
			addToHistory("Start controler [" + oRule.getRateInfo() + "] [" + oRule.getID() + "]");
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
			
			if (!ControlerState.WORK.equals(oControler.getControlerState()))
				continue;
			
			oControler.setControlerState(ControlerState.WAIT);
			addToHistory("Stop controler [" + oRateInfo + "] [" + oControler.getTradesInfo().getRuleID() + "]");
		}
	}
	
	protected boolean getIsOperationAvalible(final String strOperaion)
	{
		return (m_strOperations.equals(OPERATIONS_ALL) || m_strOperations.toLowerCase().contains(";" + strOperaion.toLowerCase() + ";"));
	}

	@Override public StockManagesInfo getInfo()
	{
		return m_oStockManagesInfo;
	}
	
	@Override public void tradeStart(final TaskTrade oTaskTrade) 
	{
		m_oStockManagesInfo.tradeStart(oTaskTrade);
		save();
	}
	
	@Override public void tradeDone(final TaskTrade oTaskTrade) 
	{
		trackTrades(oTaskTrade);
		m_oStockManagesInfo.tradeDone(oTaskTrade);
		save();
	}

	@Override public void buyDone(final TaskTrade oTaskTrade) 
	{
		m_oStockManagesInfo.buyDone(oTaskTrade);
		save();
	}
	
	@Override public void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume) 
	{
	}
	
	@Override public void addSell(final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) 
	{
	} 
	
	@Override public ManagerHistory getHistory()
	{
		return m_oManagerHistory;
	}
	
	protected void addToHistory(final String strMessage)
	{
		m_oManagerHistory.addMessage(strMessage);
	}
	
	public void save()
	{
		try 
		{
	         final FileOutputStream oFileStream = new FileOutputStream(getFileName(WorkerFactory.getStockExchange()));
	         final ObjectOutputStream oStream = new ObjectOutputStream(oFileStream);
	         oStream.writeObject(m_oStockManagesInfo);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (IOException e) 
		{
			WorkerFactory.onException("Save manager info exception", e);
		}			
	}

	public StockManagesInfo load(final IStockExchange oStockExchange)
	{
		try 
		{
	         final FileInputStream oFileStream = new FileInputStream(getFileName(oStockExchange));
	         final ObjectInputStream oStream = new ObjectInputStream(oFileStream);
	         final StockManagesInfo oStockManagesInfo = (StockManagesInfo) oStream.readObject();
	         oStream.close();
	         oFileStream.close();
	         
	         return oStockManagesInfo;
		} 
		catch (final Exception e) 
		{
			WorkerFactory.onException("Load manager info exception", e);
			return new StockManagesInfo();
	    }			
	}

	String getFileName(final IStockExchange oStockExchange)
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + oStockExchange.getStockName() + "\\manager.ser";
	}
}

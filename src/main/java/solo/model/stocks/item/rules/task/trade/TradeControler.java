package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.CurrencyAmount;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RulesFactory;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.trade.GetTradeInfoCommand;
import solo.model.stocks.item.command.trade.SetTaskParameterCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.item.rules.task.TaskFactory;
import solo.model.stocks.item.rules.task.TaskType;
import solo.model.stocks.item.rules.task.strategy.IBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.transport.MessageLevel;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TradeControler extends TaskBase implements ITradeControler
{
	protected static final double RESET_CRITICAL_PRICE_PERCENT = 0.999;
	protected static final double MIN_CRITICAL_PRICE_PERCENT = 0.995;

	private static final long serialVersionUID = 2548242166461334806L;
	
	final static public String TRADE_SUM = "#sum#";
	final static public String MAX_TARDES = "#count#";
	
	protected Integer m_nMaxTrades = 2;
	protected TradesInfo m_oTradesInfo;
	protected IBuyStrategy m_oBuyStrategy = null;

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
	
	public String getFullInfo()
	{ 
		String strInfo = m_oRateInfo.toString().toUpperCase();
		strInfo += " / buyStrategy [" + (null != getBuyStrategy() ? getBuyStrategy().getName() : "Default") + "]";
		strInfo += " / tradeCount [" + m_nMaxTrades + "]\r\n";
		return getTradesInfo().getInfo() + "\r\n" + strInfo;
	}
	
	public IBuyStrategy getBuyStrategy()
	{
		return m_oBuyStrategy;
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
	
	public String getInfo(final Integer nRuleID)
	{
		String strInfo = getType() + 
				" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo) + 
				" " + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, nRuleID, GetTradeInfoCommand.FULL_PARAMETER, StringUtils.EMPTY) + 
				"\r\n";   
		final List<ITradeTask> aTaskTrades = getTaskTrades();
		for(final ITradeTask oTaskTrade : aTaskTrades)
		{
			int nTaskRuleID = getRuleID(oTaskTrade);
			
			String strQuickSell = StringUtils.EMPTY;
			if (oTaskTrade.getTradeInfo().getTaskSide().equals(OrderSide.SELL))
			{
				final BigDecimal nNewCriticalPrice = MathUtils.getBigDecimalRoundedUp(oTaskTrade.getTradeInfo().getCriticalPrice().doubleValue() * RESET_CRITICAL_PRICE_PERCENT, TradeUtils.getPricePrecision(m_oRateInfo));
				strQuickSell = " " + CommandFactory.makeCommandLine(SetTaskParameterCommand.class, SetTaskParameterCommand.RULE_ID_PARAMETER, nTaskRuleID, 
						SetTaskParameterCommand.NAME_PARAMETER, TaskTrade.CRITICAL_PRICE_PARAMETER, 
						SetTaskParameterCommand.VALUE_PARAMETER, MathUtils.toCurrencyString(nNewCriticalPrice).replace(",", StringUtils.EMPTY));
			}
		
			strInfo += " -> " + oTaskTrade.getInfo(nTaskRuleID).replace("\r\n", "\r\n    ") + strQuickSell + "\r\n"; 
		}

		return strInfo +  
				(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}
	
	protected int getRuleID(final IRule oTask)
	{
		for(final IRule oRule : getStockExchange().getRules().getRules().values())
		{
			final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRule);
			if (null != oTradeTask && oTradeTask.equals(oTask))
				return getStockExchange().getRules().getRuleID(oRule);	

			final ITradeControler oTradeControler = TradeUtils.getRuleAsTradeControler(oRule);
			if (null != oTradeControler && oTradeControler.equals(oTask))
				return getStockExchange().getRules().getRuleID(oRule);	
		}
		
		return -1;
	}
	
	protected List<ITradeTask> getTaskTrades()
	{
		final List<ITradeTask> aTaskTrades = new LinkedList<ITradeTask>();
		for(final IRule oRule : getStockExchange().getRules().getRules().values())
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
	}
	
	protected void checkTrade(final ITradeTask oTaskTrade, boolean bIsBuyPrecent, List<ITradeTask> aTaskTrades)
	{
	}
	
	protected void createNewTrade(final StateAnalysisResult oStateAnalysisResult, List<ITradeTask> aTaskTrades)
	{
		try
		{
			final BigDecimal oMinTradeVolume = TradeUtils.getMinTradeVolume(m_oRateInfo);
			final BigDecimal oBuyPrice = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getBidsOrders().get(0).getPrice();
			final BigDecimal oMinTradeSum = oMinTradeVolume.multiply(oBuyPrice).multiply(new BigDecimal(1.01)); 

			final BigDecimal nTotalSum = m_oTradesInfo.getSum().add(m_oTradesInfo.getSumToSell());
			if (nTotalSum.compareTo(oMinTradeSum) < 0)
				return;

			BigDecimal nBuySum = MathUtils.getRoundedBigDecimal(nTotalSum.doubleValue() / m_nMaxTrades, TradeUtils.getVolumePrecision(m_oRateInfo));
			final CurrencyAmount oCurrencyAmount = getStockSource().getUserInfo(m_oRateInfo).getMoney().get(m_oRateInfo.getCurrencyTo());
			if (null != oCurrencyAmount && oCurrencyAmount.getBalance().compareTo(nBuySum) < 0)
				nBuySum = oCurrencyAmount.getBalance();

			if (nBuySum.compareTo(oMinTradeSum) < 0)
			{
				nBuySum = oMinTradeSum;
				if (null != oCurrencyAmount && oCurrencyAmount.getBalance().compareTo(nBuySum) < 0)
					return;
			}
			
			final String strRuleInfo = "task" + "_" + m_oRateInfo + "_" + TaskType.TRADE.toString().toLowerCase() + "_" + nBuySum;
			final TaskFactory oTask = (TaskFactory) RulesFactory.getRule(strRuleInfo);
			final TaskTrade oTaskTrade = ((TaskTrade)oTask.getTaskBase());
			
			if (m_oTradesInfo.getFreeVolume().compareTo(BigDecimal.ZERO) > 0)
			{
				final BigDecimal nNeedSellVolume = m_oTradesInfo.getFreeVolume();
				oTaskTrade.getTradeInfo().addBuy(BigDecimal.ZERO, nNeedSellVolume);
			}
			if (null != getBuyStrategy())
				oTaskTrade.getTradeInfo().setBuyStrategy(getBuyStrategy());
			
			oTaskTrade.setTradeControler(this);
			getStockExchange().getRules().addRule(oTaskTrade);
		}
		catch(final Exception e) 
		{
			System.err.printf(Thread.currentThread().getName() +  " Thread exception : " + CommonUtils.getExceptionMessage(e) + "\r\n");
		}
	}

	public void remove()
	{
		final List<ITradeTask> aTaskTrades = getTaskTrades();
		for(final ITradeTask oTaskTrade : aTaskTrades)
			oTaskTrade.setTradeControler(ITradeControler.NULL);
	}
	
	public void tradeDone(final TaskTrade oTaskTrade)
	{
		getTradesInfo().incTradeCount();

		final List<ITradeTask> aTaskTrades = getTaskTrades();
		m_oTradesInfo.updateOrderInfo(aTaskTrades);
		
		sendMessage(MessageLevel.TRADERESULT, getType() + " " + m_oRateInfo.toString() + "\r\n" + getTradesInfo().getInfo());
	}	

	public void buyDone(final TaskTrade oTaskTrade) 
	{
	}

	public void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume) 
	{
		getTradesInfo().addBuy(nSpendSum, nBuyVolume);
		if (nSpendSum.compareTo(BigDecimal.ZERO) != 0 || nBuyVolume.compareTo(BigDecimal.ZERO) != 0)
			sendMessage(MessageLevel.DEBUG, m_oRateInfo.toString() +  " Buy : " + MathUtils.toCurrencyString(nSpendSum) + " / " + MathUtils.toCurrencyStringEx(nBuyVolume));
	}
	
	public void addSell(final BigDecimal nReceivedSum, final BigDecimal nSoldVolume) 
	{
		getTradesInfo().addSell(nReceivedSum, nSoldVolume);
		if (nReceivedSum.compareTo(BigDecimal.ZERO) != 0 || nSoldVolume.compareTo(BigDecimal.ZERO) != 0)
			sendMessage(MessageLevel.DEBUG, m_oRateInfo.toString() +  " Sell : " + MathUtils.toCurrencyString(nReceivedSum) + " / " + MathUtils.toCurrencyStringEx(nSoldVolume));
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
}

package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;
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
import solo.transport.MessageLevel;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TradeControler extends TaskBase implements ITradeControler
{
	protected static final double RESET_CRITICAL_PRICE_PERCENT = 0.999;

	private static final long serialVersionUID = 2548242166461334806L;
	
	final static public String TRADE_SUM = "#sum#";
	final static public String MAX_TARDES = "#count#";
	
	protected Integer m_nMaxTrades = 2;
	protected TradesInfo m_oTradesInfo;
	protected List<ITradeTask> m_oControlTasks = new LinkedList<ITradeTask>();

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
	
	@Override public String getType()
	{
		return "CONTROLER";   
	}
	
	public TradesInfo getTradesInfo()
	{
		if (null == m_oTradesInfo)
			m_oTradesInfo = new TradesInfo();
		return m_oTradesInfo;   
	}
	
	public String getInfo(final Integer nRuleID)
	{
		String strInfo = getType() + 
				" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo) + 
				" " + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, nRuleID, GetTradeInfoCommand.FULL_PARAMETER, true) + 
				"\r\n";   
		final List<TaskTrade> aTaskTrades = getTaskTrades();
		int nTaskRuleID = -1;
		for(final TaskTrade oTaskTrade : aTaskTrades)
		{
			for(final IRule oRule : getStockExchange().getRules().getRules().values())
			{
				if (!(oRule instanceof TaskFactory))
					continue;
					
				final TaskBase oTask = ((TaskFactory)oRule).getTaskBase();
				if (!(oTask instanceof TaskTrade))
					continue;
					
				if (oTask.equals(oTaskTrade))
					nTaskRuleID = getStockExchange().getRules().getRuleID(oRule);	
			}
			
			String strQuickSell = StringUtils.EMPTY;
			if (oTaskTrade.getTradeInfo().getTaskSide().equals(OrderSide.SELL))
			{
				final BigDecimal nNewCriticalPrice = MathUtils.getBigDecimalRoundedUp(oTaskTrade.getTradeInfo().getCriticalPrice().doubleValue() * RESET_CRITICAL_PRICE_PERCENT, TradeUtils.DEFAULT_PRICE_PRECISION);
				strQuickSell = " " + CommandFactory.makeCommandLine(SetTaskParameterCommand.class, SetTaskParameterCommand.RULE_ID_PARAMETER, nTaskRuleID, 
						SetTaskParameterCommand.NAME_PARAMETER, TaskTrade.CRITICAL_PRICE_PARAMETER, 
						SetTaskParameterCommand.VALUE_PARAMETER, MathUtils.toCurrencyString(nNewCriticalPrice).replace(",", StringUtils.EMPTY)) + "\r\n";
			}
		
			strInfo += " -> " + (oTaskTrade.getTradeInfo().getOrder().equals(Order.NULL) ?  oTaskTrade.getTradeInfo().getTaskSide() + "/" : StringUtils.EMPTY) + 
					oTaskTrade.getTradeInfo().getOrder().getInfoShort() +
					strQuickSell +
					" " + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, nTaskRuleID, GetTradeInfoCommand.FULL_PARAMETER, true) + "\r\n"; 
		}

		return strInfo +  
				(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}
	
	protected List<TaskTrade> getTaskTrades()
	{
		final List<TaskTrade> aTaskTrades = new LinkedList<TaskTrade>();
		for(final IRule oRule : getStockExchange().getRules().getRules().values())
		{
			if (!(oRule instanceof TaskFactory))
				continue;
				
			final TaskBase oTask = ((TaskFactory)oRule).getTaskBase();
			if (!(oTask instanceof TaskTrade))
				continue;
				
			final TaskTrade oTaskTrade = (TaskTrade)oTask;
			if (this.equals(oTaskTrade.getTradeControler()))
				aTaskTrades.add((TaskTrade)oTask);	
		}
		return aTaskTrades;
	}
	
	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		final List<TaskTrade> aTaskTrades = getTaskTrades();
		boolean bIsBuyPrecent = false;
		for(final TaskTrade oTaskTrade : aTaskTrades)
			bIsBuyPrecent |= oTaskTrade.getTradeInfo().getTaskSide().equals(OrderSide.BUY);

		for(final TaskTrade oTaskTrade : aTaskTrades)
			checkTrade(oTaskTrade, bIsBuyPrecent, aTaskTrades);

		if (aTaskTrades.size() < m_nMaxTrades && !bIsBuyPrecent)
			createNewTrade();
	}
	
	protected void checkTrade(final TaskTrade oTaskTrade, boolean bIsBuyPrecent, List<TaskTrade> aTaskTrades)
	{
	}
	
	protected void createNewTrade()
	{
		try
		{
			final String strRuleInfo = "task" + "_" + m_oRateInfo + 
					"_" + TaskType.TRADE.toString().toLowerCase() + "_" + m_oTradesInfo.getBuySum();
			final TaskFactory oTaskTrade = (TaskFactory) RulesFactory.getRule(strRuleInfo);
			((TaskTrade)oTaskTrade.getTaskBase()).setTradeControler(this);
			getStockExchange().getRules().addRule(oTaskTrade);
		}
		catch(final Exception e) 
		{
			System.err.printf(Thread.currentThread().getName() +  " Thread exception : " + CommonUtils.getExceptionMessage(e) + "\r\n");
		}
	}
	
	public void tradeDone(final TaskTrade oTaskTrade)
	{
		getTradesInfo().incTradeCount();
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
				
		super.setParameter(strParameterName, strValue);
	}
}

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
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.GetRulesCommand;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.trade.GetTradeInfoCommand;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.item.rules.task.TaskFactory;
import solo.model.stocks.item.rules.task.TaskType;
import solo.utils.CommonUtils;

public class TradeControler extends TaskBase implements ITradeControler
{
	private static final long serialVersionUID = 2548242166461334806L;
	
	final static public String TRADE_VOLUME = "#volume#";
	
	protected BigDecimal m_oTradeVolume; 
	protected Integer m_nMaxTrades = 2;
	protected TradesInfo m_oTradesInfo;
	protected List<ITradeTask> m_oControlTasks = new LinkedList<ITradeTask>();

	public TradeControler(RateInfo oRateInfo, String strCommandLine)
	{
		super(oRateInfo, strCommandLine, TRADE_VOLUME);
		m_oTradeVolume = getParameterAsBigDecimal(TRADE_VOLUME);
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
		
			strInfo += " -> " + (oTaskTrade.getTradeInfo().getOrder().equals(Order.NULL) ?  oTaskTrade.getTradeInfo().getTaskSide() + "/" : StringUtils.EMPTY) + 
					oTaskTrade.getTradeInfo().getOrder().getInfoShort() +    
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
		if (aTaskTrades.size() >= m_nMaxTrades)
			return;
		
		boolean bIsBuyPrecent = false;
		for(final TaskTrade oTaskTrade : aTaskTrades)
			bIsBuyPrecent |= oTaskTrade.getTradeInfo().getTaskSide().equals(OrderSide.BUY);
			
		if (!bIsBuyPrecent)
			createNewTrade();
	}
	
	protected void createNewTrade()
	{
		try
		{
			final String strRuleInfo = "task" + "_" + m_oRateInfo + 
					"_" + TaskType.TRADE.toString().toLowerCase() + "_" + m_oTradeVolume;
			final TaskFactory oTaskTrade = (TaskFactory) RulesFactory.getRule(strRuleInfo);
			((TaskTrade)oTaskTrade.getTaskBase()).setTradeControler(this);
			getStockExchange().getRules().addRule(oTaskTrade);
			
			sendMessage("Trade new trade. " + BaseCommand.getCommand(GetRulesCommand.NAME));
		}
		catch(final Exception e) 
		{
			System.err.printf(Thread.currentThread().getName() +  " Thread exception : " + CommonUtils.getExceptionMessage(e) + "\r\n");
		}
	}
	
	public void tradeDone(final TaskTrade oTaskTrade)
	{
		getTradesInfo().incTradeCount();
		getTradesInfo().addSpendSum(oTaskTrade.getTradeInfo().getSpendSum());
		getTradesInfo().addReceivedSum(oTaskTrade.getTradeInfo().getReceivedSum());

		sendMessage(getType() + "\r\n" + getTradesInfo().getInfo());
	}	

	public void buyDone(final TaskTrade oTaskTrade) 
	{
	}
}

package solo.model.stocks.item.command.rule;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.ISystemCommand;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class CheckRulesCommand extends BaseCommand implements ISystemCommand
{
	final static public String NAME = "checkRules";

	public CheckRulesCommand()
	{
		super(NAME, StringUtils.EMPTY);
	}

	public CheckRulesCommand(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = getStockExchange();
		final List<Entry<Integer, IRule>> aCheckRulesInfo = new LinkedList<Entry<Integer, IRule>>();
		for(final Entry<Integer, IRule> oRuleInfo : oStockExchange.getRules().getRules().entrySet())
		{
			final IRule oRule = oRuleInfo.getValue();
			final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRule);
			if (null != oTradeTask && !oTradeTask.getTradeControler().equals(ITradeControler.NULL))
				continue;
			
			aCheckRulesInfo.add(oRuleInfo);
		}
		if (aCheckRulesInfo.size() == 0)
			return;
		
		final MainWorker oMainWorker = WorkerFactory.getMainWorker();
		final StateAnalysisResult oStateAnalysisResult = oStockExchange.getHistory().getLastAnalysisResult();
		final ExecutorService oThreadPool = Executors.newFixedThreadPool(aCheckRulesInfo.size());
		for (int nThreadPos = 0; nThreadPos < aCheckRulesInfo.size(); nThreadPos++) 
		{
			final Entry<Integer, IRule> oRuleInfo = aCheckRulesInfo.get(nThreadPos);
			final CheckRuleThread oLoadRateThread = new CheckRuleThread(oRuleInfo.getValue(), oStateAnalysisResult, oMainWorker, oRuleInfo.getKey());
			oThreadPool.submit(oLoadRateThread);
		};
		oThreadPool.shutdown();
			
		try 
		{
			oThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} 
		catch (InterruptedException e) 
		{
			WorkerFactory.getMainWorker().onException(e);
		}
		
	}
}

class CheckRuleThread implements Runnable 
{
	final IRule m_oRule;
	final StateAnalysisResult m_oStateAnalysisResult;
	final Integer m_nRuleID;
	final MainWorker m_oMainWorker;

    public CheckRuleThread(final IRule oRule, final StateAnalysisResult oStateAnalysisResult, final MainWorker oMainWorker, Integer nRuleID)
    {
    	m_oRule = oRule;
    	m_oStateAnalysisResult = oStateAnalysisResult;
    	m_oMainWorker = oMainWorker;
    	m_nRuleID = nRuleID;
    }

    public void run() 
    {
		try
		{
			Thread.currentThread().setName("Check rule #" + m_nRuleID);
			WorkerFactory.registerMainWorkerThread(Thread.currentThread().getId(), m_oMainWorker);
			m_oRule.check(m_oStateAnalysisResult, m_nRuleID);	
		}
		catch (final Exception e)
		{
			WorkerFactory.getMainWorker().onException(e);
		}
    }
}
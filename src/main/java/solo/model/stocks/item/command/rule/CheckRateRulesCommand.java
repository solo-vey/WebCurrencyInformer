package solo.model.stocks.item.command.rule;

import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.ISystemCommand;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class CheckRateRulesCommand extends BaseCommand implements ISystemCommand
{
	public static final String NAME = "checkRateRules";
	
	protected RateInfo m_oRateInfo;

	public CheckRateRulesCommand()
	{
		super(NAME, StringUtils.EMPTY);
	}

	public CheckRateRulesCommand(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
	}
	
	public CheckRateRulesCommand(final RateInfo oRateInfo)
	{
		super(NAME, StringUtils.EMPTY);
		m_oRateInfo = oRateInfo;
	}
	
	@Override public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
		
		final RateState oRateState = WorkerFactory.getStockSource().getRateState(m_oRateInfo);
		WorkerFactory.getStockTestSource().getRateState(m_oRateInfo);
		oStockExchange.getLastAnalysisResult().analyse(oRateState, oStockExchange, m_oRateInfo);
		
		final RateInfo oReverseRateInfo = RateInfo.getReverseRate(m_oRateInfo);
		final RateState oReverseRateState = makeReverseRateState(oRateState);
		oStockExchange.getLastAnalysisResult().analyse(oReverseRateState, oStockExchange, oReverseRateInfo);
		
		final StateAnalysisResult oStateAnalysisResult = oStockExchange.getLastAnalysisResult();
		final List<Entry<Integer, IRule>> oRules = oStockExchange.getRules().getRules(m_oRateInfo);
		for(final Entry<Integer, IRule> oRuleInfo : oRules)
			oRuleInfo.getValue().check(oStateAnalysisResult);
		
		final List<Entry<Integer, IRule>> oReverseRules = oStockExchange.getRules().getRules(oReverseRateInfo);
		for(final Entry<Integer, IRule> oReverseRuleInfo : oReverseRules)
			oReverseRuleInfo.getValue().check(oStateAnalysisResult);
		
		/*final MainWorker oMainWorker = WorkerFactory.getMainWorker();
		final ExecutorService oThreadPool = Executors.newFixedThreadPool(oRules.size());
		for (int nThreadPos = 0; nThreadPos < oRules.size(); nThreadPos++) 
		{
			final Entry<Integer, IRule> oRuleInfo = oRules.get(nThreadPos);
			final CheckRuleThread oLoadRateThread = new CheckRuleThread(oRuleInfo.getValue(), oStateAnalysisResult, oMainWorker);
			oThreadPool.submit(oLoadRateThread);
		}
		oThreadPool.shutdown();
			
		try 
		{
			oThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} 
		catch (InterruptedException e) 
		{
			WorkerFactory.onException("CheckRateRulesCommand.execute()", e);
		}*/
	}
	
	public static RateState makeReverseRateState(final RateState oRateState)
	{
		final RateState oReverseRateState = new RateState(RateInfo.getReverseRate(oRateState.getRateInfo()));
		for(final Order oOrder : oRateState.getBidsOrders())
			oReverseRateState.getAsksOrders().add(TradeUtils.makeReveseOrder(oOrder));
		for(final Order oOrder : oRateState.getAsksOrders())
			oReverseRateState.getBidsOrders().add(TradeUtils.makeReveseOrder(oOrder));
		for(final Order oOrder : oRateState.getTrades())
			oReverseRateState.getTrades().add(TradeUtils.makeReveseOrder(oOrder));
		return oReverseRateState;
	}
}

class CheckRuleThread implements Runnable 
{
	final IRule m_oRule;
	final StateAnalysisResult m_oStateAnalysisResult;
	final MainWorker m_oMainWorker;

    public CheckRuleThread(final IRule oRule, final StateAnalysisResult oStateAnalysisResult, final MainWorker oMainWorker)
    {
    	m_oRule = oRule;
    	m_oStateAnalysisResult = oStateAnalysisResult;
    	m_oMainWorker = oMainWorker;
    }

    public void run() 
    {
		try
		{
			Thread.currentThread().setName("Check rule #" + m_oRule.getID());
			WorkerFactory.registerMainWorkerThread(Thread.currentThread().getId(), m_oMainWorker);
			m_oRule.check(m_oStateAnalysisResult);	
		}
		catch (final Exception e)
		{
			WorkerFactory.onException("Check rule #" + m_oRule.getID(), e);
		}
    }
}
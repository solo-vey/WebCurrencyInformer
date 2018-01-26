package solo.model.stocks.worker;

import java.util.List;
import java.util.Map.Entry;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.rule.CheckRateRulesCommand;

public class StockRateWorker extends BaseWorker
{
	final protected RateInfo m_oRateInfo;
	final MainWorker m_oMainWorker; 
	
	public StockRateWorker(final MainWorker oMainWorker, final RateInfo oRateInfo, final int nTimeOut)
	{
		super(nTimeOut, oMainWorker.getStock());
		m_oRateInfo = oRateInfo;
		m_oMainWorker = oMainWorker;
	}

	public void startWorker()
	{
		super.startWorker();
		WorkerFactory.registerMainWorkerThread(getId(), m_oMainWorker);
		Thread.currentThread().setName(m_oMainWorker.getStockExchange().getStockName() + " - " + m_oRateInfo);
	}
	
	@Override protected void doWork() throws Exception
	{
		super.doWork();
		
		addCommand(new CheckRateRulesCommand(m_oRateInfo));
	}
	
	@Override int getTimeOut()
	{
		final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
		final List<Entry<Integer, IRule>> oRules = oStockExchange.getRules().getRules(m_oRateInfo);
		
		return (oRules.size() > 0 ? m_nTimeOut : m_nTimeOut * 10);
	}
}

package solo.model.stocks.worker;

import java.util.List;
import java.util.Map.Entry;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.rule.CheckRateRulesCommand;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;

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
	
	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}

	public void startWorker()
	{
		super.startWorker();
		WorkerFactory.registerMainWorkerThread(getId(), m_oMainWorker);
	}
	
	@Override protected void doWork() throws Exception
	{
		Thread.currentThread().setName(m_oMainWorker.getStockExchange().getStockName() + " - " + m_oRateInfo);
		super.doWork();
		
		addCommand(new CheckRateRulesCommand(m_oRateInfo));
	}
	
	@Override int getTimeOut()
	{
		final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
		final List<Entry<Integer, IRule>> oRules = oStockExchange.getRules().getRules(m_oRateInfo);
		final boolean bIsHasRealRules = ManagerUtils.isHasRealRules(m_oRateInfo);
		
		return (oRules.size() > 0 ? (bIsHasRealRules ? m_nTimeOut : m_nTimeOut * 3) : m_nTimeOut * 10);
	}
	
	@Override public boolean equals(Object oObject)
	{
		if (null == oObject)
			return false;
		
		if (!(oObject instanceof StockRateWorker))
			return false;
		
		final StockRateWorker oStockRateWorker = (StockRateWorker)oObject;
		return m_oRateInfo.equals(oStockRateWorker.m_oRateInfo) && m_oMainWorker.equals(oStockRateWorker.m_oMainWorker);
	};
}

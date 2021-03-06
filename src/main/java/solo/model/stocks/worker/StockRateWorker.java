package solo.model.stocks.worker;

import java.util.List;
import java.util.Map.Entry;

import org.java_websocket.client.WebSocketClient;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.rule.CheckRateRulesCommand;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.transport.websocket.WebSocketClientTransport;

public class StockRateWorker extends BaseWorker
{
	protected final RateInfo m_oRateInfo;
	final MainWorker m_oMainWorker; 
	WebSocketClient publicWs;
	
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

	@Override public void startWorker()
	{
		super.startWorker();
		WorkerFactory.registerMainWorkerThread(getId(), m_oMainWorker);
		publicWs = WebSocketClientTransport.createPublicWs(getRateInfo());
	}
	
	@Override protected void doWork() throws Exception
	{
		Thread.currentThread().setName(m_oMainWorker.getStockExchange().getStockName() + " - " + m_oRateInfo);
		addCommand(new CheckRateRulesCommand(m_oRateInfo));
		super.doWork();
		
		if (null != publicWs && publicWs.isClosed())
			publicWs = WebSocketClientTransport.createPublicWs(getRateInfo());
	}
	
	@Override int getTimeOut()
	{
		final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
		final List<Entry<Integer, IRule>> oRules = oStockExchange.getRules().getRules(m_oRateInfo);
		final boolean bIsHasRealWorkingRules = ManagerUtils.isHasRealWorkingControlers(m_oRateInfo);
		
		return (oRules.size() > 0 ? (bIsHasRealWorkingRules ? m_nTimeOut : m_nTimeOut * 3) : m_nTimeOut * 10);
	}
	
	@Override public boolean equals(Object oObject)
	{
		if (null == oObject)
			return false;
		
		if (!(oObject instanceof StockRateWorker))
			return false;
		
		final StockRateWorker oStockRateWorker = (StockRateWorker)oObject;
		return m_oRateInfo.equals(oStockRateWorker.m_oRateInfo) && m_oMainWorker.equals(oStockRateWorker.m_oMainWorker);
	}
}

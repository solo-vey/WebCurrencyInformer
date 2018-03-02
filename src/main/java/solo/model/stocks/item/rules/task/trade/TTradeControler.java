package solo.model.stocks.item.rules.task.trade;

import java.util.List;

import solo.model.stocks.worker.WorkerFactory;

public class TTradeControler extends TradeControler implements ITest
{
	private static final long serialVersionUID = -2333740994826596628L;
	
	public static final String NAME = "TESTCONTROLER";

	public TTradeControler(String strCommandLine)
	{
		super(strCommandLine);
	}
	
	@Override public TradesInfo getTradesInfo()
	{
		if (null == m_oTradesInfo)
			m_oTradesInfo = new TestTradesInfo(m_oRateInfo, m_nID);

		return m_oTradesInfo;   
	}

	@Override protected String getTraderName()
	{
		return TTaskTrade.NAME;
	}
	
	@Override public String getInfo()
	{
		return "TEST" + super.getInfo();
	}
	
	@Override public ControlerState getControlerState()
	{
		return ControlerState.WORK;
	}
	
	@Override public void setControlerState(ControlerState oControlerState)
	{
	} 
	
	public void remove()
	{
		super.remove();
		
		final List<ITradeTask> aTaskTrades = getTaskTrades();
		for(final ITradeTask oTaskTrade : aTaskTrades)
			WorkerFactory.getStockExchange().getRules().removeRule(oTaskTrade);
	}
}

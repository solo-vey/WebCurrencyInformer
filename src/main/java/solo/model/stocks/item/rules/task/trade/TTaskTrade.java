package solo.model.stocks.item.rules.task.trade;

import solo.model.stocks.worker.WorkerFactory;

public class TTaskTrade extends TaskTrade implements ITest
{
	public static final String NAME = "TESTTRADE";

	private static final long serialVersionUID = 4204959432012712932L;

	public TTaskTrade(String strCommandLine) throws Exception
	{
		super(strCommandLine);
	}

	@Override public String getType()
	{
		return NAME;   
	}

	@Override public TradeInfo getTradeInfo()
	{
		if (null == m_oTradeInfo)
			m_oTradeInfo = new TestTradeInfo(m_oRateInfo, WorkerFactory.getStockExchange().getRules().getNextRuleID());
		
		return m_oTradeInfo;	
	}
}

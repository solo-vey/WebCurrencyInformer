package solo.model.stocks.item.command.system;

import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

public class RemoveRateCommand extends BaseCommand
{
	final static public String NAME = "removeRate";

	final static public String RATE_PARAMETER = "#rate#";

	final protected RateInfo m_oRateInfo; 

	public RemoveRateCommand(final String strСommandLine)
	{
		super(strСommandLine, RATE_PARAMETER);
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		WorkerFactory.getMainWorker().getStockExchange().getStockSource().removeRate(m_oRateInfo);
		WorkerFactory.getMainWorker().getStockWorker().stopRateWorker(m_oRateInfo);
		
		WorkerFactory.getMainWorker().sendSystemMessage("Stock rate worker [" + m_oRateInfo + "] stopped");
	}
}

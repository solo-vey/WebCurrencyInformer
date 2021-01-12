package solo.model.stocks.item.command.system;

import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

public class AddRateCommand extends BaseCommand
{
	public static final String NAME = "addRate";

	public static final String RATE_PARAMETER = "#rate#";

	protected final RateInfo m_oRateInfo; 

	public AddRateCommand(final String strСommandLine)
	{
		super(strСommandLine, RATE_PARAMETER);
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		WorkerFactory.getStockSource().registerRate(m_oRateInfo);
		WorkerFactory.getMainWorker().getStockWorker().startRateWorker(m_oRateInfo);
		
		WorkerFactory.getMainWorker().sendSystemMessage("Stock rate worker [" + m_oRateInfo + "] started");
	}
}

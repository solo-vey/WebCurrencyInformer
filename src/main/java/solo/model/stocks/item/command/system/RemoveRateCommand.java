package solo.model.stocks.item.command.system;

import java.util.Map.Entry;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.worker.WorkerFactory;

public class RemoveRateCommand extends BaseCommand
{
	public static final String NAME = "removeRate";

	public static final String RATE_PARAMETER = "#rate#";

	protected final RateInfo m_oRateInfo; 

	public RemoveRateCommand(final String strСommandLine)
	{
		super(strСommandLine, RATE_PARAMETER);
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		WorkerFactory.getStockSource().removeRate(m_oRateInfo);
		WorkerFactory.getMainWorker().getStockWorker().stopRateWorker(m_oRateInfo);
		
		if (!ManagerUtils.isHasRealControlers(m_oRateInfo))
		{
			for(final Entry<Integer, IRule> oRuleInfo : WorkerFactory.getStockExchange().getRules().getRules(m_oRateInfo))
				WorkerFactory.getStockExchange().getRules().removeRule(oRuleInfo.getValue());
		}
		
		WorkerFactory.getMainWorker().sendSystemMessage("Stock rate worker [" + m_oRateInfo + "] stopped");
	}
}

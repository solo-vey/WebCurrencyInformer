package solo.model.stocks.item.command.rule;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class AddControlerCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "addControler";

	final static public String RATE_PARAMETER = "#rate#";
	final static public String SUM_PARAMETER = "#sum#";
	
	final protected RateInfo m_oRateInfo; 
	final protected BigDecimal m_nSum; 
	
	public AddControlerCommand(final String strRuleInfo)
	{
		super(strRuleInfo, CommonUtils.mergeParameters(RATE_PARAMETER, SUM_PARAMETER));
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
		m_nSum = getParameterAsBigDecimal(SUM_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final String strMessage = ManagerUtils.createTradeControler(m_oRateInfo, m_nSum);
		if (StringUtils.isEmpty(strMessage))
			WorkerFactory.getMainWorker().sendSystemMessage("Controler [" + m_oRateInfo + "] sum [" + m_nSum + "] added");
		else
			WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}
}

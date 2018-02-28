package solo.model.stocks.item.command.system;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class SetStockParameterCommand extends BaseCommand
{
	final static public String NAME = "setStockParameter";
	final static public String NAME_PARAMETER = "#name#";
	final static public String VALUE_PARAMETER = "#value#";
	
	final protected String m_strName;  
	final protected String m_strValue;  
	
	public SetStockParameterCommand(final String strСommandLine)
	{
		super(strСommandLine, CommonUtils.mergeParameters(NAME_PARAMETER, VALUE_PARAMETER));
		m_strName = getParameter(NAME_PARAMETER).replace("-", "_");
		m_strValue = getParameter(VALUE_PARAMETER).replace("-", "_");
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		if (!"?".equalsIgnoreCase(m_strValue))
			WorkerFactory.getStockExchange().setParameter(m_strName, m_strValue);
		
		WorkerFactory.getMainWorker().sendSystemMessage("Parameter [" + m_strName + "] = [" + WorkerFactory.getStockExchange().getParameter(m_strName) + "]");
	}
}

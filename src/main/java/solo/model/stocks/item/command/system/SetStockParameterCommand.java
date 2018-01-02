package solo.model.stocks.item.command.system;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class SetStockParameterCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "setStockParameter";
	final static public String NAME_PARAMETER = "#name#";
	final static public String VALUE_PARAMETER = "#value#";
	
	final protected String m_strName;  
	final protected String m_strValue;  
	
	public SetStockParameterCommand(final String strСommandLine)
	{
		super(strСommandLine, CommonUtils.mergeParameters(NAME_PARAMETER, VALUE_PARAMETER));
		m_strName = getParameter(NAME_PARAMETER);
		m_strValue = getParameter(VALUE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		getStockExchange().setParameter(m_strName, m_strValue);
		sendMessage("Parameter [" + m_strName + "] = [" + getStockExchange().getParameter(m_strName) + "]");
	}
}

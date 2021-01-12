package solo.model.stocks.item.command.system;

import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.CommonUtils;
import solo.utils.ResourceUtils;

/** Формат комманды 
 */
public class SetStockParameterCommand extends BaseCommand
{
	public static final String NAME = "setStockParameter";
	public static final String NAME_PARAMETER = "#name#";
	public static final String VALUE_PARAMETER = "#value#";
	
	protected final String m_strName;  
	protected final String m_strValue;  
	
	public SetStockParameterCommand(final String strСommandLine)
	{
		super(strСommandLine, CommonUtils.mergeParameters(NAME_PARAMETER, VALUE_PARAMETER));
		m_strName = getParameter(NAME_PARAMETER).replace("-", "_");
		m_strValue = getParameter(VALUE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
	
		if (m_strName.contains("?"))
		{
			final String strProperyFile = WorkerFactory.getStockExchange().getStockProperties();
			final Properties oProperties = new Properties();
			oProperties.load(ResourceUtils.class.getClassLoader().getResourceAsStream(strProperyFile));
			
			String strMessage = StringUtils.EMPTY;
			for(final Entry<Object, Object> oProperty : oProperties.entrySet())
				strMessage += oProperty.getKey() + "=" + WorkerFactory.getStockExchange().getParameter(oProperty.getKey().toString()) + "\r\n";

			WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
		}
		else
		{
			if (!"?".equalsIgnoreCase(m_strValue))
				WorkerFactory.getStockExchange().setParameter(m_strName, m_strValue);
			WorkerFactory.getMainWorker().sendSystemMessage("Parameter [" + m_strName + "] = [" + WorkerFactory.getStockExchange().getParameter(m_strName) + "]");
		}
	}
}

package solo.model.stocks.item.command;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

abstract public class BaseCommand implements ICommand
{
	final protected String m_strCommandInfo;
	final static protected List<String> s_oHistory =  Collections.synchronizedList(new LinkedList<String>()); 
	
	public BaseCommand()
	{
		this(StringUtils.EMPTY);
	}
	
	public BaseCommand(final String strCommandInfo)
	{
		m_strCommandInfo = strCommandInfo;
	}
	
	public static List<String> getHistory()
	{
		return s_oHistory;
	}
	
	public void execute() throws Exception
	{
		if (!(this instanceof IHistoryCommand))
			return;
		
		final String strFullCommand = CommandFactory.getCommandName(getClass()) + 
			(StringUtils.isNotBlank(m_strCommandInfo) ? "_" + m_strCommandInfo.replace(" ", "_") : StringUtils.EMPTY);
		s_oHistory.remove(strFullCommand);
		if (s_oHistory.size() > 200)
			s_oHistory.remove(0);
		
		s_oHistory.add(strFullCommand);
	}
	
	public String getInfo()
	{
		return m_strCommandInfo;
	}
	
	public static String getCommand(final String strTemplate)
	{
		return "/" + strTemplate;
	}
	
	public static String getCommand(final String strTemplate, final Object ... aParameters)
	{
		return String.format("/" + strTemplate, aParameters);
	}
}

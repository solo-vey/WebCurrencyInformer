package solo.model.stocks.item.command.base;

import org.apache.commons.lang.StringUtils;

abstract public class BaseCommand extends HasParameters implements ICommand
{
	public BaseCommand(final String strRuleInfo, final String strParametersTemplate)
	{
		super(strRuleInfo, strParametersTemplate);
	}

	public String getHelp() throws Exception
	{
		return "/" + CommandFactory.getCommandName(getClass()) + (StringUtils.isNotBlank(getTemplate()) ? "_" + getTemplate() : StringUtils.EMPTY);
	}

	public void execute() throws Exception
	{
	}
	
	public String getInfo()
	{
		return m_strCommandInfo;
	}
	
	public static String getCommand(final String strTemplate)
	{
		return "/" + strTemplate;
	}
}

package solo.model.stocks.item;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.system.HelpCommand;
import solo.model.stocks.item.rules.notify.EventFactory;
import solo.model.stocks.item.rules.task.TaskFactory;
import solo.utils.CommonUtils;

public class RulesFactory
{
	protected static Map<String, Class<?>> s_oRulesClassByType = new HashMap<String, Class<?>>();
	
	static
	{
		registerRuleClass("event", EventFactory.class);
		registerRuleClass("task",  TaskFactory.class);
	}
	
	static protected void registerRuleClass(final String strRule, final Class<?> oClass)
	{
		s_oRulesClassByType.put(strRule, oClass);
	}
	
	public static Map<String, Class<?>> getAllRuleTypes()
	{
		return s_oRulesClassByType;
	}

	public static IRule getRule(final String strCommandLine)
	{
		final String strRule = CommonUtils.splitFirst(strCommandLine).toLowerCase();
		final Class<?> oClass = (Class<?>) s_oRulesClassByType.get(strRule);
		if (null == oClass)
			return null;
		
		try
		{
			final String strRuleArguments = CommonUtils.splitTail(strCommandLine).toLowerCase();
			final Constructor<?> oConstructor = oClass.getConstructor(String.class);
			return (IRule) oConstructor.newInstance(new Object[] { strRuleArguments });
		}
		catch(final Exception e) 
		{
			System.err.print(e);
		}

		return null;
	}
	
	public static String getHelp(final String strCommandStart, final String strType)
	{
		String strHelp = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(strType))
		{
			for(final Entry<String, Class<?>> oRuleType : getAllRuleTypes().entrySet())
			{
				if (StringUtils.isNotBlank(strType) && !strType.equalsIgnoreCase(oRuleType.getKey()))
					continue;
				
				final String strCommand = strCommandStart.replace("#type#", oRuleType.getKey());
				final String strRuleHelp = getRule(oRuleType.getKey()).getHelp(strCommand); 
				strHelp += strRuleHelp;
			}
		}
		else
		{
			for(final Entry<String, Class<?>> oRuleType : getAllRuleTypes().entrySet())
			{
				final String strCommand = strCommandStart.replace("#type#", oRuleType.getKey()).replace("/", StringUtils.EMPTY);
				strHelp += CommandFactory.makeCommandLine(HelpCommand.class, HelpCommand.COMMAND_PARAMETER, strCommand) + "\r\n";
			}
		}
		
		return strHelp;
	}
}

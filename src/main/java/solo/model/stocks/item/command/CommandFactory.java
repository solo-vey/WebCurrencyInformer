package solo.model.stocks.item.command;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.utils.CommonUtils;

public class CommandFactory
{
	protected static Map<String, Class<?>> s_oCommandClassByType = new HashMap<String, Class<?>>();
	
	static
	{
		registerCommandClass(HelpCommand.NAME, 				HelpCommand.class);
		registerCommandClass(HistoryCommand.NAME, 			HistoryCommand.class);
		
		registerCommandClass(GetRateInfoCommand.NAME, GetRateInfoCommand.class);
		registerCommandClass(LoadRateInfoCommand.NAME, LoadRateInfoCommand.class);
		
		registerCommandClass(SendMessageCommand.NAME, 			SendMessageCommand.class);
		registerCommandClass(GetTransportMessagesCommand.NAME,	GetTransportMessagesCommand.class);

		registerCommandClass(AddRuleCommand.NAME, 		AddRuleCommand.class);
		registerCommandClass(RemoveRuleCommand.NAME,	RemoveRuleCommand.class);
		registerCommandClass(GetRulesCommand.NAME,		GetRulesCommand.class);
		registerCommandClass(CheckRulesCommand.NAME,	CheckRulesCommand.class);
		registerCommandClass(RemoveAllRulesCommand.NAME,RemoveAllRulesCommand.class);

		registerCommandClass(GetStockInfoCommand.NAME, 	GetStockInfoCommand.class);
		registerCommandClass(RemoveOrderCommand.NAME, 	RemoveOrderCommand.class);
		registerCommandClass(AddOrderCommand.NAME, 		AddOrderCommand.class);
	}
	
	static protected void registerCommandClass(final String strCommand, final Class<?> oClass)
	{
		s_oCommandClassByType.put(strCommand.toLowerCase(), oClass);
	}

	static public Map<String, Class<?>> getAllCommands()
	{
		return s_oCommandClassByType;
	}
	
	static public ICommand getCommand(final String strCommandLine)
	{
		final String strCommand = CommonUtils.splitFirst(strCommandLine).toLowerCase().replace("command:///", StringUtils.EMPTY).replace("/", StringUtils.EMPTY);
		final Class<?> oClass = (Class<?>) s_oCommandClassByType.get(strCommand);
		if (null == oClass)
			return new UnknownCommand("Unknown command [" + strCommandLine + "]");
		
		try
		{
			final String strCommandArguments = CommonUtils.splitTail(strCommandLine).toLowerCase();
			final Constructor<?> oConstructor = oClass.getConstructor(String.class);
			return (ICommand) oConstructor.newInstance(new Object[] { strCommandArguments });
		}
		catch(final Exception e) 
		{
			return new UnknownCommand("Exception execute command " + strCommandLine + ". " + e.getMessage());
		}
	}

	public static String getCommandName(final Class<?> oClass)
	{
		for(final Entry<String, Class<?>> oCommandInfo : s_oCommandClassByType.entrySet())
		{
			if (oCommandInfo.getValue().equals(oClass))
				return oCommandInfo.getKey();
		}
		
		return "Uknown";
	}
}

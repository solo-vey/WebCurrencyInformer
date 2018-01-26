package solo.model.stocks.item.command.base;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.trade.AddOrderCommand;
import solo.model.stocks.item.command.trade.GetStockInfoCommand;
import solo.model.stocks.item.command.trade.GetTradeInfoCommand;
import solo.model.stocks.item.command.trade.ManageStock;
import solo.model.stocks.item.command.trade.RemoveOrderCommand;
import solo.model.stocks.item.command.trade.SetTaskParameterCommand;
import solo.model.stocks.item.command.trade.StockRestartCommand;
import solo.model.stocks.item.command.rule.AddRuleCommand;
import solo.model.stocks.item.command.rule.CheckRateRulesCommand;
import solo.model.stocks.item.command.rule.GetRulesCommand;
import solo.model.stocks.item.command.rule.RemoveAllRulesCommand;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.command.system.GetRateChartCommand;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.system.GetTransportMessagesCommand;
import solo.model.stocks.item.command.system.HelpCommand;
import solo.model.stocks.item.command.system.HistoryCommand;
import solo.model.stocks.item.command.system.LastErrorsCommand;
import solo.model.stocks.item.command.system.SendMessageCommand;
import solo.model.stocks.item.command.system.SetStockParameterCommand;
import solo.model.stocks.item.command.system.StartStockCommand;
import solo.model.stocks.item.command.system.StopStockCommand;
import solo.model.stocks.item.command.system.UnknownCommand;
import solo.utils.CommonUtils;

public class CommandFactory
{
	protected static Map<String, Class<?>> s_oCommandClassByType = new HashMap<String, Class<?>>();
	protected static Map<CommandGroup, List<String>> s_oCommandInGroup = new HashMap<CommandGroup, List<String>>();
	
	static
	{
		registerCommandClass(HelpCommand.NAME, 	HelpCommand.class, CommandGroup.OTHER);
		registerCommandClass(HistoryCommand.NAME, HistoryCommand.class, CommandGroup.OTHER);
		registerCommandClass(LastErrorsCommand.NAME, LastErrorsCommand.class, CommandGroup.OTHER);
		
		registerCommandClass(GetRateInfoCommand.NAME, GetRateInfoCommand.class, CommandGroup.INFO);
		registerCommandClass(GetRateChartCommand.NAME, GetRateChartCommand.class, CommandGroup.INFO);

		registerCommandClass(SendMessageCommand.NAME, 			SendMessageCommand.class, CommandGroup.SYSTEM);
		registerCommandClass(GetTransportMessagesCommand.NAME,	GetTransportMessagesCommand.class, CommandGroup.SYSTEM);
		registerCommandClass(CheckRateRulesCommand.NAME,	CheckRateRulesCommand.class, CommandGroup.SYSTEM);
		registerCommandClass(SetStockParameterCommand.NAME, SetStockParameterCommand.class, CommandGroup.SYSTEM);
		registerCommandClass(StartStockCommand.NAME, StartStockCommand.class, CommandGroup.SYSTEM);
		registerCommandClass(StopStockCommand.NAME, StopStockCommand.class, CommandGroup.SYSTEM);

		registerCommandClass(AddRuleCommand.NAME, 		AddRuleCommand.class, CommandGroup.RULES);
		registerCommandClass(GetRulesCommand.NAME,		GetRulesCommand.class, CommandGroup.RULES);
		registerCommandClass(RemoveRuleCommand.NAME,	RemoveRuleCommand.class, CommandGroup.RULES);
		registerCommandClass(RemoveAllRulesCommand.NAME,RemoveAllRulesCommand.class, CommandGroup.RULES);

		registerCommandClass(GetStockInfoCommand.NAME, 		GetStockInfoCommand.class, CommandGroup.INFO);
		registerCommandClass(AddOrderCommand.NAME, 			AddOrderCommand.class, CommandGroup.TRADE);
		registerCommandClass(RemoveOrderCommand.NAME, 		RemoveOrderCommand.class, CommandGroup.TRADE);
		registerCommandClass(StockRestartCommand.NAME, 		StockRestartCommand.class, CommandGroup.SYSTEM);
		registerCommandClass(GetTradeInfoCommand.NAME, 		GetTradeInfoCommand.class, CommandGroup.TRADE);
		registerCommandClass(SetTaskParameterCommand.NAME, 	SetTaskParameterCommand.class, CommandGroup.TRADE);
		registerCommandClass(ManageStock.NAME,				ManageStock.class,  CommandGroup.TRADE);	
	}
	
	static protected void registerCommandClass(final String strCommand, final Class<?> oClass, final CommandGroup oCommandGroup)
	{
		s_oCommandClassByType.put(strCommand.toLowerCase(), oClass);
		if (!s_oCommandInGroup.containsKey(oCommandGroup))
			s_oCommandInGroup.put(oCommandGroup, new LinkedList<String>());
		s_oCommandInGroup.get(oCommandGroup).add(strCommand.toLowerCase());
	}

	static public Map<String, Class<?>> getAllCommands()
	{
		return s_oCommandClassByType;
	}

	static public Map<CommandGroup, List<String>> getAllCommandsGroup()
	{
		return s_oCommandInGroup;
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
	
	public static String makeCommandLine(final Class<?> oClass, final Object ... aParameters)
	{
		String strCommand = "/" + getCommandName(oClass);
		final ICommand oCommand = getCommand(strCommand);
		strCommand += "_" + ((BaseCommand)oCommand).getTemplate(); 

		final int nParameterCount = aParameters.length / 2;
		for(int nPos = 0; nPos < nParameterCount; nPos++)
			strCommand = strCommand.replace(aParameters[nPos * 2].toString(), (null != aParameters[nPos * 2 + 1] ? aParameters[nPos * 2 + 1].toString() : StringUtils.EMPTY));
		return strCommand;
	}
	
}

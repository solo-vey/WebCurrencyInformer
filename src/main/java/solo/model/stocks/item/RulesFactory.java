package solo.model.stocks.item;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.system.HelpCommand;
import solo.model.stocks.item.rules.task.trade.BuyTaskTrade;
import solo.model.stocks.item.rules.task.trade.BuyTradeControler;
import solo.model.stocks.item.rules.task.trade.SellTaskTrade;
import solo.model.stocks.item.rules.task.trade.TTaskTrade;
import solo.model.stocks.item.rules.task.trade.TTradeControler;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.utils.CommonUtils;

public class RulesFactory
{
	protected static Map<String, Class<?>> s_oRulesClassByType = new HashMap<String, Class<?>>();
	
	static
	{
		registerRuleClass(TaskTrade.NAME.toLowerCase(),  		TaskTrade.class);
		registerRuleClass(BuyTaskTrade.NAME.toLowerCase(), 	 	BuyTaskTrade.class);
		registerRuleClass(SellTaskTrade.NAME.toLowerCase(), 	SellTaskTrade.class);
		registerRuleClass(TTaskTrade.NAME.toLowerCase(),  		TTaskTrade.class);
		
		registerRuleClass(TradeControler.NAME.toLowerCase(),  	TradeControler.class);
		registerRuleClass(TTradeControler.NAME.toLowerCase(),  	TTradeControler.class);
		registerRuleClass(BuyTradeControler.NAME.toLowerCase(), BuyTradeControler.class);
	}
	
	static protected void registerRuleClass(final String strTaskType, final Class<?> oClass)
	{
		s_oRulesClassByType.put(strTaskType, oClass);
	}
	
	public static Map<String, Class<?>> getAllRuleTypes()
	{
		return s_oRulesClassByType;
	}

	public static IRule getRule(final String strCommandLine) throws Exception
	{
		final String strTaskType = CommonUtils.splitFirst(strCommandLine).toLowerCase();
		final Class<?> oClass = (Class<?>) s_oRulesClassByType.get(strTaskType);
		if (null == oClass)
			return null;
		
		final String strRuleArguments = CommonUtils.splitTail(strCommandLine).toLowerCase();
		
		final Constructor<?> oConstructor = oClass.getConstructor(String.class);
		return (IRule) oConstructor.newInstance(new Object[] {strRuleArguments });
	}
	
	public static String getHelp(final String strCommandStart, final String strType) throws Exception
	{
		String strHelp = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(strType))
		{
			for(final Entry<String, Class<?>> oRuleType : getAllRuleTypes().entrySet())
			{
				if (StringUtils.isNotBlank(strType) && !strType.equalsIgnoreCase(oRuleType.getKey()))
					continue;
				
				final String strCommand = strCommandStart.replace("#type#", oRuleType.getKey());
				final String strRuleHelp = getRule(oRuleType.getKey().toString()).getHelp(strCommand); 
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

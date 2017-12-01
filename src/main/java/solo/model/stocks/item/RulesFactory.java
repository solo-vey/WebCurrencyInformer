package solo.model.stocks.item;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import solo.model.stocks.item.rules.notify.EventFactory;
import solo.utils.CommonUtils;

public class RulesFactory
{
	protected static Map<String, Class<?>> m_oRulesClassByType = new HashMap<String, Class<?>>();
	
	static
	{
		registerRuleClass("event", EventFactory.class);
	}
	
	static protected void registerRuleClass(final String strRule, final Class<?> oClass)
	{
		m_oRulesClassByType.put(strRule, oClass);
	}

	public static IRule getRule(final String strCommandLine)
	{
		final String strRule = CommonUtils.splitFirst(strCommandLine).toLowerCase();
		final Class<?> oClass = (Class<?>) m_oRulesClassByType.get(strRule);
		if (null == oClass)
			return null;
		
		try
		{
			final String strRuleArguments = CommonUtils.splitTail(strCommandLine).toLowerCase();
			final Constructor<?> oConstructor = oClass.getConstructor(String.class);
			return (IRule) oConstructor.newInstance(new Object[] { strRuleArguments });
		}
		catch(final Exception e) {}

		return null;
	}
}

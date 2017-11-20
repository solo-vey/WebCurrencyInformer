package solo.model.sources;

import java.util.HashMap;
import java.util.Map;

import solo.model.currency.Currency;

/** Информация о всех источниках курсов */
public class SourceFactory
{
	protected final static Map<String, ISource> s_oSources = new HashMap<String, ISource>(); 
	
	/** Список отслеживаемых валют */
	static
	{
		s_oSources.put(Currency.BTC + "->" + Currency.UAH, new BestChange(Currency.BTC, Currency.UAH));
	}
}

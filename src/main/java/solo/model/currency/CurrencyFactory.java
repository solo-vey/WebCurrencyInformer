package solo.model.currency;

import java.util.HashMap;
import java.util.Map;

/** Информация о всех отслеживаемых валютах */
public class CurrencyFactory
{
	protected final static Map<Currency, CurrencyInfo> s_oCurrencies = new HashMap<Currency, CurrencyInfo>(); 
	
	/** Список отслеживаемых валют */
	static
	{
		s_oCurrencies.put(Currency.BTC, new CurrencyInfo(Currency.BTC));
		s_oCurrencies.put(Currency.ETH, new CurrencyInfo(Currency.ETH));
	}
}

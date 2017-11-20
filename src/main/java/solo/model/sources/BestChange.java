package solo.model.sources;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyRate;
import solo.utils.RequestUtils;

public class BestChange implements ISource
{
	/** URL получение информации */
	final static String INFO_URL = "https://www.bestchange.ru/action.php";
	/** Шаблон для п */
	final static String SELL_TEMPLATE = "<div class=\"fs\">([\\d|\\s|\\.]+)<small>";
	/** URL получение информации */
	final static String BUY_TEMPLATE = "<td class=\"bi\">([\\d|\\s|\\.]+)<small>";
	
	/** Получение по валюте ее кода в данном источнике даннных */
	final static Map<Currency, String> m_sCurrency2Code = new HashMap<Currency, String>();
	
	/** С какой валюты */
	final private Currency m_oCurrencyFrom;
	/** В какую валюту */
	final private Currency m_oCurrencyTo;
	
	/** Получение по валюте ее кода в данном источнике даннных */
	static
	{
		m_sCurrency2Code.put(Currency.BTC, "93");
		m_sCurrency2Code.put(Currency.ETH, "139");
		m_sCurrency2Code.put(Currency.UAH, "56");
	}
	
	/** Конструктор
	 * @param strForm С валюты
	 * @param strTo В валюту */
	public BestChange(final Currency oCurrencyForm, final Currency oCurrencyTo)
	{
		m_oCurrencyFrom = oCurrencyForm;
		m_oCurrencyTo = oCurrencyTo;
	}

	/** Получение по валюте ее кода в данном источнике даннных   
	 * @param oCurrencyForm Валюта
	 * @return Код валюты в данном источнике даннных */
	private String getCode(final Currency oCurrency)
	{
		return m_sCurrency2Code.get(oCurrency);
	}

	/** Получение данных о курсе продажи валюте  
	 * @throws Exception */
	@Override public CurrencyRate getSellRate() throws Exception
	{
		final Map<String, String> aParameters = getDefaultParameters();
		aParameters.put("from", getCode(m_oCurrencyFrom));
		aParameters.put("to", getCode(m_oCurrencyTo));
		aParameters.put("sort", "to");
		aParameters.put("range", "desc");
		final String strResult = RequestUtils.sendPost(INFO_URL, aParameters , true);
		return new CurrencyRate(m_oCurrencyFrom, m_oCurrencyTo, parseResult(strResult, ActionType.SELL));
	}

	/** Получение данных о курсе покупки валюте  
	 * @throws Exception */
	@Override public CurrencyRate getBuyRate() throws Exception
	{
		final Map<String, String> aParameters = getDefaultParameters();
		aParameters.put("from", getCode(m_oCurrencyTo));
		aParameters.put("to", getCode(m_oCurrencyFrom));
		aParameters.put("sort", "from");
		aParameters.put("range", "ask");
		
		final String strResult = RequestUtils.sendPost(INFO_URL, aParameters , true);
		return new CurrencyRate(m_oCurrencyTo, m_oCurrencyFrom, parseResult(strResult, ActionType.BUY));
	}

	/** Получение данных о валюте  
	 * @throws Exception */
	Map<String, String> getDefaultParameters()
	{
		final Map<String, String> aParameters = new HashMap<String, String>();
		aParameters.put("action", "getrates");
		aParameters.put("page", "rates");
		aParameters.put("city", "0");
		aParameters.put("commission", "0");
		
		return aParameters;
	}
	
	/** Разбираем полученные данные 
	 * @param strData Данные в виде HTML
	 * @return Первое в списке значение валюты */
	double parseResult(final String strData, final ActionType oType)
	{
		final String strFindTemplate = (oType.equals(ActionType.BUY) ? SELL_TEMPLATE : BUY_TEMPLATE);
		final Matcher oMatcher = Pattern.compile(strFindTemplate).matcher(strData);
		double nValue = (oType.equals(ActionType.BUY) ? Double.MAX_VALUE : 0); 
		while (oMatcher.find())
		{
			double nCurrentValue = Double.parseDouble(oMatcher.group(1).replace(" ", ""));
			if (oType.equals(ActionType.BUY))
				nValue = (nValue > nCurrentValue ? nCurrentValue : nValue);
			else
				nValue = (nValue < nCurrentValue ? nCurrentValue : nValue);
		}
		return nValue;
	}

}

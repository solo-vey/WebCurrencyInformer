package solo.model.sources;

import solo.model.currency.CurrencyRate;

/** Интерфейс для получения данных о валюте */
public interface ISource
{
	/** Получение данных о курсе продажи валюте  
	 * @throws Exception */
	CurrencyRate getSellRate() throws Exception;
	/** Получение данных о курсе покупки валюте  
	 * @throws Exception */
	CurrencyRate getBuyRate() throws Exception;
}

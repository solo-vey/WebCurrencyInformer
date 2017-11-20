package solo.model.currency;

import solo.model.sources.ActionType;

/** Информация об отслеживании курса валюты  */
public class TrackInfo
{
	/** Отслеживаемый курс */
	final private CurrencyRate m_oTrackCurrencyRate;
	/** Тип действия с валютой */
	private ActionType m_oActionType;
	
	/** Конструктор 
	 * @param oCurrency Валюта */
	public TrackInfo(final CurrencyRate oTrackCurrencyRate, final ActionType oActionType)
	{
		m_oTrackCurrencyRate = oTrackCurrencyRate; 
		m_oActionType = oActionType;
	}
	
	/** @return Отслеживаемый курс */
	public CurrencyRate getTrackCurrencyRate()
	{
		return m_oTrackCurrencyRate;
	}
	
	/** @return Тип действия с валютой */
	public ActionType getActionType()
	{
		return m_oActionType;
	}
	
	/** Проверка текущего курса валюты
	 * @param oTrackCurrencyRate Текущий курс валюты
	 * @return True - достигли отслеживаемой точки, false - недостигли отслеживаемой точки */
	public boolean checkRate(final CurrencyRate oCurrencyRate)
	{
		if (m_oActionType.equals(ActionType.BUY) && oCurrencyRate.getValue() < m_oTrackCurrencyRate.getValue())
			return true;
		
		if (m_oActionType.equals(ActionType.SELL) && oCurrencyRate.getValue() > m_oTrackCurrencyRate.getValue())
			return true;
		
		return false;
	}
}

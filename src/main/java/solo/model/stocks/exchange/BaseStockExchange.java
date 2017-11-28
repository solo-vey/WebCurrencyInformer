package solo.model.stocks.exchange;

import java.util.HashMap;
import java.util.Map;

import solo.model.currency.Currency;
import solo.model.stocks.item.Events;
import solo.model.stocks.item.StockCurrencyVolume;
import solo.model.stocks.source.IStockSource;

public class BaseStockExchange implements IStockExchange
{
	final protected String m_strStockName;
	final protected String m_strStockProperies;
	protected IStockSource m_oStockSource;
	final protected Map<Currency, StockCurrencyVolume> m_oStockCurrencyVolumes = new HashMap<Currency, StockCurrencyVolume>(); 
	final protected Events m_oEvents = new Events(this);
	
	public BaseStockExchange(final String strStockName, final String strStockProperies)
	{
		m_strStockName = strStockName;
		m_strStockProperies = strStockProperies;
	}
	
	public String getStockName()
	{
		return m_strStockName;
	}
	
	public String getStockProperties()
	{
		return m_strStockProperies;
	}
	
	public IStockSource getStockSource()
	{
		return m_oStockSource;
	}
	
	public StockCurrencyVolume getStockCurrencyVolume(final Currency oCurrency)
	{
		return m_oStockCurrencyVolumes.get(oCurrency);
	}
	
	public Events getEvents()
	{
		return m_oEvents;
	}
}

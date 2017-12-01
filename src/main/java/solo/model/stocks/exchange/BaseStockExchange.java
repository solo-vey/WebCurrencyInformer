package solo.model.stocks.exchange;

import java.util.HashMap;
import java.util.Map;

import solo.model.currency.Currency;
import solo.model.stocks.analyse.IStateAnalysis;
import solo.model.stocks.analyse.SimpleStateAnalysis;
import solo.model.stocks.history.StockRateStatesLocalHistory;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.StockCurrencyVolume;
import solo.model.stocks.oracle.IRateOracle;
import solo.model.stocks.oracle.SimpleRateOracle;
import solo.model.stocks.source.IStockSource;
import ua.lz.ep.utils.ResourceUtils;

public class BaseStockExchange implements IStockExchange
{
	final protected String m_strStockName;
	final protected String m_strStockProperies;
	protected IStockSource m_oStockSource;
	final protected Map<Currency, StockCurrencyVolume> m_oStockCurrencyVolumes = new HashMap<Currency, StockCurrencyVolume>(); 
	final protected Rules m_oRules = new Rules(this);
	final StockRateStatesLocalHistory m_oStockRateStatesLocalHistory;
	final IStateAnalysis m_oStateAnalysis = new SimpleStateAnalysis();

	public BaseStockExchange(final String strStockName, final String strStockProperies)
	{
		m_strStockName = strStockName;
		m_strStockProperies = strStockProperies;

		final IRateOracle oRateOracle = new SimpleRateOracle();
		final int nHistoryLength = ResourceUtils.getIntFromResource("history.length", getStockProperties(), 100);
		final int nForecastLength = ResourceUtils.getIntFromResource("forecast.length", getStockProperties(), 1);
		m_oStockRateStatesLocalHistory = new StockRateStatesLocalHistory(nHistoryLength, nForecastLength, oRateOracle);
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
	
	public StockRateStatesLocalHistory getHistory()
	{
		return m_oStockRateStatesLocalHistory;
	}
	
	public IStateAnalysis getAnalysis()
	{
		return m_oStateAnalysis;
	}
	
	public StockCurrencyVolume getStockCurrencyVolume(final Currency oCurrency)
	{
		return m_oStockCurrencyVolumes.get(oCurrency);
	}
	
	public Rules getRules()
	{
		return m_oRules;
	}
}

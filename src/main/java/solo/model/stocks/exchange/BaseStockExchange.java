package solo.model.stocks.exchange;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.stocks.analyse.IStateAnalysis;
import solo.model.stocks.analyse.SimpleStateAnalysis;
import solo.model.stocks.history.StockRateStatesLocalHistory;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.StockCurrencyVolume;
import solo.model.stocks.item.analyse.StockCandlestick;
import solo.model.stocks.item.rules.task.manager.IStockManager;
import solo.model.stocks.source.IStockSource;
import solo.transport.MessageLevel;
import ua.lz.ep.utils.ResourceUtils;

public class BaseStockExchange implements IStockExchange
{
	private static final String MESSAGE_LEVEL_PARAMETER = "messageLevel";
	
	protected MessageLevel m_oMessageLevel = MessageLevel.TRADERESULT;
	
	final protected String m_strStockName;
	final protected String m_strStockProperies;
	protected IStockSource m_oStockSource;
	final protected Map<Currency, StockCurrencyVolume> m_oStockCurrencyVolumes = new HashMap<Currency, StockCurrencyVolume>(); 
	final protected Rules m_oRules;
	final StockRateStatesLocalHistory m_oStockRateStatesLocalHistory;
	final IStateAnalysis m_oStateAnalysis = new SimpleStateAnalysis();
	final IStockManager m_oStockManager = IStockManager.NULL;
	final StockCandlestick m_oStockCandlestick;

	public BaseStockExchange(final String strStockName, final String strStockProperies)
	{
		m_strStockName = strStockName;
		m_strStockProperies = strStockProperies;

		final int nHistoryLength = ResourceUtils.getIntFromResource("history.length", getStockProperties(), 100);
		m_oStockRateStatesLocalHistory = new StockRateStatesLocalHistory(nHistoryLength);
		m_oRules = new Rules(this);

		final int nCandleDurationMinutes = ResourceUtils.getIntFromResource("candle.duration_minutes", getStockProperties(), 5);
		m_oStockCandlestick = new StockCandlestick(this, nCandleDurationMinutes);
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
	
	public StockCandlestick getStockCandlestick()
	{
		return m_oStockCandlestick;
	}
	
	public IStateAnalysis getAnalysis()
	{
		return m_oStateAnalysis;
	}
	
	public StockCurrencyVolume getStockCurrencyVolume(final Currency oCurrency)
	{
		return (null != m_oStockCurrencyVolumes.get(oCurrency) ? m_oStockCurrencyVolumes.get(oCurrency) : new StockCurrencyVolume(oCurrency, 1.0));
	}
	
	public Rules getRules()
	{
		return m_oRules;
	}
	
	public MessageLevel getMessageLevel()
	{
		return m_oMessageLevel;
	}
	
	public void setParameter(String strName, String strValue)
	{
		if (strName.equalsIgnoreCase(MESSAGE_LEVEL_PARAMETER))
			m_oMessageLevel = MessageLevel.valueOf(strValue.toUpperCase());
	}
	
	public String getParameter(String strName)
	{
		if (strName.equalsIgnoreCase(MESSAGE_LEVEL_PARAMETER))
			return m_oMessageLevel.toString();
		
		return StringUtils.EMPTY;
	}

	@Override public IStockManager getManager()
	{
		return m_oStockManager;
	}
}

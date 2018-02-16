package solo.model.stocks.exchange;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.IStateAnalysis;
import solo.model.stocks.analyse.SimpleStateAnalysis;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.analyse.StockCandlestick;
import solo.model.stocks.item.rules.task.manager.IStockManager;
import solo.model.stocks.item.rules.task.manager.StockManager;
import solo.model.stocks.source.IStockSource;
import solo.transport.MessageLevel;
import solo.utils.ResourceUtils;

public class BaseStockExchange implements IStockExchange
{
	private static final String MESSAGE_LEVEL_PARAMETER = "messageLevel";
	
	protected MessageLevel m_oMessageLevel = MessageLevel.TRADERESULT;
	
	final protected String m_strStockName;
	final protected String m_strStockProperies;
	protected IStockSource m_oStockSource;
	final protected Rules m_oRules;
	final StateAnalysisResult m_oLastAnalysisResult;
	final IStateAnalysis m_oStateAnalysis = new SimpleStateAnalysis();
	final IStockManager m_oStockManager;
	final StockCandlestick m_oStockCandlestick;

	public BaseStockExchange(final String strStockName, final String strStockProperies)
	{
		m_strStockName = strStockName;
		m_strStockProperies = strStockProperies;

		m_oLastAnalysisResult = new StateAnalysisResult();
		m_oRules = new Rules(this);
		m_oStockManager = new StockManager(this);

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
	
	public StateAnalysisResult getLastAnalysisResult()
	{
		return m_oLastAnalysisResult;
	}
	
	public StockCandlestick getStockCandlestick()
	{
		return m_oStockCandlestick;
	}
	
	public IStateAnalysis getAnalysis()
	{
		return m_oStateAnalysis;
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

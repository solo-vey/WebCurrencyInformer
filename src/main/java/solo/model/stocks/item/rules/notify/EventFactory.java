package solo.model.stocks.item.rules.notify;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.HasParameters;
import solo.utils.CommonUtils;

public class EventFactory extends HasParameters implements IRule
{
	private static final long serialVersionUID = 908092157964890096L;
	
	final static public String RATE_PARAMETER = "#rate#";
	final static public String EVENT_TYPE_PARAMETER = "#eventType#";

	protected static Map<EventType, Class<?>> s_oEventClassByType = new HashMap<EventType, Class<?>>();

	final protected EventBase m_oEventBase;
	
	static
	{
		registerEventClass(EventType.SELL,  EventSell.class);
		registerEventClass(EventType.BUY,   EventBuy.class);
		registerEventClass(EventType.TRADE, EventTrade.class);

		registerEventClass(EventType.SELLTRACE,  EventSellTrace.class);
		registerEventClass(EventType.BUYTRACE, 	 EventBuyTrace.class);
		registerEventClass(EventType.TRADETRACE, EventTradeTrace.class);

		registerEventClass(EventType.TRENDTRACE, EventTrendTrace.class);
	}
	
	static protected void registerEventClass(final EventType oEventType, final Class<?> oClass)
	{
		s_oEventClassByType.put(oEventType, oClass);
	}
	
	static public EventBase getEvent(final EventType oEventType, final RateInfo oRateInfo, final String strPriceInfo) throws Exception
	{
		final Class<?> oClass = (Class<?>) s_oEventClassByType.get(oEventType);
		if (null == oClass)
			return null;
		
		final Constructor<?> oConstructor = oClass.getConstructor(RateInfo.class, String.class);
		return (EventBase) oConstructor.newInstance(new Object[] { oRateInfo, strPriceInfo });
	}
	
	public EventFactory(final String strCommandLine) throws Exception
	{
		super(strCommandLine, CommonUtils.mergeParameters(RATE_PARAMETER, EVENT_TYPE_PARAMETER, TAIL_PARAMETER));
		final RateInfo oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
		final EventType oType = (EventType) getParameterAsEnum(EVENT_TYPE_PARAMETER, EventType.class);
		final String strPriceInfo = getParameter(TAIL_PARAMETER);
		m_oEventBase = getEvent(oType, oRateInfo, strPriceInfo);
	}
	
	public String getHelp(final String strCommandStart) throws Exception
	{
		String strHelp = StringUtils.EMPTY;
		for(final Entry<EventType, Class<?>> oEventInfo : s_oEventClassByType.entrySet())
		{
			final String strCommandStartForEventType = CommonUtils.mergeParameters(strCommandStart, RATE_PARAMETER, oEventInfo.getKey().toString().toLowerCase());
			strHelp += getEvent(oEventInfo.getKey(), null, StringUtils.EMPTY).getHelp(strCommandStartForEventType) + "\r\n";
		}
		
		return strHelp;
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return (null != m_oEventBase ? m_oEventBase.getInfo(nRuleID) : "/removeRule_" + nRuleID + "\r\n");
	}
	
	public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		if (null != m_oEventBase)
			m_oEventBase.check(oStateAnalysisResult, nRuleID);
	}
	
	public void remove()
	{
		if (null != m_oEventBase)
			m_oEventBase.remove();
	}
}


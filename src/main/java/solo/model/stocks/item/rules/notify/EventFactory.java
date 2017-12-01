package solo.model.stocks.item.rules.notify;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import solo.model.currency.Currency;
import solo.model.stocks.BaseObject;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.utils.CommonUtils;

public class EventFactory extends BaseObject implements IRule
{
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
	}
	
	static protected void registerEventClass(final EventType oEventType, final Class<?> oClass)
	{
		s_oEventClassByType.put(oEventType, oClass);
	}
	
	static public EventBase getEvent(final EventType oEventType, final RateInfo oRateInfo, final String strPriceInfo)
	{
		final Class<?> oClass = (Class<?>) s_oEventClassByType.get(oEventType);
		if (null == oClass)
			return null;
		
		try
		{
			final Constructor<?> oConstructor = oClass.getConstructor(RateInfo.class, String.class);
			return (EventBase) oConstructor.newInstance(new Object[] { oRateInfo, strPriceInfo });
		}
		catch(final Exception e) 
		{
			return null;
		}
	}
	
	public EventFactory(final String strCommandLine)
	{
		final Currency oCurrency = Currency.valueOf(CommonUtils.splitFirst(strCommandLine).toUpperCase());
		final RateInfo oRateInfo = new RateInfo(oCurrency, Currency.UAH);
		final EventType oType = EventType.valueOf(CommonUtils.splitToPos(strCommandLine, 1).toUpperCase());
		final String strPriceInfo = CommonUtils.splitTail(strCommandLine, 3);
		m_oEventBase = getEvent(oType, oRateInfo, strPriceInfo);
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
}


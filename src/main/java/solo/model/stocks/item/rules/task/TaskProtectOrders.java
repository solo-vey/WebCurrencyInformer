package solo.model.stocks.item.rules.task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.TrendType;
import solo.model.stocks.oracle.RateForecast;
import solo.model.stocks.oracle.RatesForecast;

public class TaskProtectOrders extends TaskBase
{
	private static final long serialVersionUID = -178152243757975169L;

	private static final int TIMEOUT_RELOAD_ORDERS_SEC = 60;
	
	protected Date m_oNextDateReloadOrders = null;
	protected List<Order> m_aOrders;

	public TaskProtectOrders(final RateInfo oRateInfo, final String strCommandLine) throws Exception
	{
		super(oRateInfo, strCommandLine);
	}

	private void reloadOrders()
	{
		if (null != m_oNextDateReloadOrders && m_oNextDateReloadOrders.after(new Date()))
			return;
		
		try
		{
			final StockUserInfo oUserInfo = getStockSource().getUserInfo(m_oRateInfo);
			m_aOrders = oUserInfo.getOrders().get(m_oRateInfo);
			
			final Calendar oCalendar = Calendar.getInstance();
			oCalendar.setTime(new Date());
			oCalendar.add(Calendar.SECOND, TIMEOUT_RELOAD_ORDERS_SEC);
		    m_oNextDateReloadOrders = oCalendar.getTime();
		}
		catch (Exception e)
		{
			System.err.printf(e.getMessage());
		}
	}

	@Override public String getType()
	{
		return "PROTECTORDERS";   
	}
	
	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		reloadOrders();

		final List<RatesForecast> oForecasts = getStockExchange().getHistory().getFuture();
		if (null == oForecasts || oForecasts.size() == 0 || null == m_aOrders)
			return;
		
		final RateForecast oForecast = oForecasts.get(0).getForecust(m_oRateInfo);
		for(final Order oOrder : m_aOrders)
		{
			if (oOrder.getSide().equals(OrderSide.SELL) && oForecast.getTrendType().equals(TrendType.FAST_GROWTH))
			{
				final String strMessage = TrendType.FAST_GROWTH + " Order " + m_oRateInfo.getCurrencyFrom() + "/" + oOrder.getInfo(); 
				sendMessage(strMessage);
			}

			if (oOrder.getSide().equals(OrderSide.BUY) && oForecast.getTrendType().equals(TrendType.FAST_FALL))
			{
				final String strMessage = TrendType.FAST_GROWTH + " Order " + m_oRateInfo.getCurrencyFrom() + "/" + oOrder.getInfo(); 
				sendMessage(strMessage);
			}
		}
	}
}


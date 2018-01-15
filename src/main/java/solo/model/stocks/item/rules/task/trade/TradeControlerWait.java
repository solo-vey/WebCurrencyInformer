package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.item.analyse.CandlestickType;
import solo.model.stocks.item.analyse.StockCandlestick;
import solo.utils.MathUtils;

public class TradeControlerWait extends TradeControler
{
	private static final long serialVersionUID = 2548242566461334806L;

	final static public String TRADE_WAIT = "#wait#";
	
	public Date m_oCreateAfterDate;
	public int m_nMinutes = 2;
	
	public TradeControlerWait(RateInfo oRateInfo, String strCommandLine)
	{
		super(oRateInfo, strCommandLine, TRADE_WAIT);
		if (StringUtils.isNotBlank(getParameter(TRADE_WAIT)))
			m_nMinutes = getParameterAsInt(TRADE_WAIT);
		m_oCreateAfterDate = new Date();
	}
	
	@Override public String getType()
	{
		return "CONTROLERWAIT";   
	}
	
	@Override public String getFullInfo()
	{ 
		String strInfo = super.getFullInfo();
		strInfo += " / wait [" + m_nMinutes + "]";
		return strInfo;
	}
	
	@Override protected void checkTrade(final ITradeTask oTaskTrade, boolean bIsBuyPrecent, List<ITradeTask> aTaskTrades)
	{
		if (bIsBuyPrecent || aTaskTrades.size() < m_nMaxTrades)
			return;
		
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(m_oRateInfo);
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
	    if (oOrder.getVolume().compareTo(nMinTradeVolume) < 0)
	    	return;
	    
		final Date oMaxDateCreate = DateUtils.addMinutes(new Date(), -15); 
	    if (null == oOrder.getCreated() || oOrder.getCreated().after(oMaxDateCreate))
	    	return;
		
		final StockCandlestick oStockCandlestick = getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(m_oRateInfo);
		final CandlestickType oCandlestickType = oCandlestick.getType();
		if (!oCandlestickType.isCalm())
			return;
	    
    	final BigDecimal nNewCriticalPrice = oCandlestick.getAverageMaxPrice(3);
		final BigDecimal nAveragedBoughPrice = oTaskTrade.getTradeInfo().getAveragedBoughPrice();
    	final BigDecimal nMinCriticalPrice = MathUtils.getBigDecimal(nAveragedBoughPrice.doubleValue() * 0.998, TradeUtils.getPricePrecision(m_oRateInfo));
    	
    	if (nNewCriticalPrice.compareTo(nMinCriticalPrice) > 0)
    	{
    		oTaskTrade.getTradeInfo().setCriticalPrice(nNewCriticalPrice);
    		sendMessage(getType() + "\r\n" + oTaskTrade.getInfo(null) + "\r\n" +
    				"Reset critical price " + MathUtils.toCurrencyString(nNewCriticalPrice));
    	}
	}
	
	protected void createNewTrade(final StateAnalysisResult oStateAnalysisResult, List<ITradeTask> aTaskTrades)
	{
		final StockCandlestick oStockCandlestick = getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(m_oRateInfo);
		final CandlestickType oCandlestickType = oCandlestick.getType();
		if (oCandlestickType.isFall())
			return;
		
		super.createNewTrade(oStateAnalysisResult, aTaskTrades);
		m_oCreateAfterDate = null;
	}

	public void buyDone(final TaskTrade oTaskTrade) 
	{
		m_oCreateAfterDate = null;			
	}

	public void setNewCreateAfter(final StateAnalysisResult oStateAnalysisResult, List<ITradeTask> aTaskTrades) 
	{
		boolean bIsSellPrecent = false;
		for(final ITradeTask oTaskTrade : aTaskTrades)
			bIsSellPrecent |= oTaskTrade.getTradeInfo().getTaskSide().equals(OrderSide.SELL);
		
		final Calendar oCalendar = Calendar.getInstance();
	    oCalendar.setTime(new Date());
	    if (bIsSellPrecent)
	    	oCalendar.add(Calendar.MINUTE, m_nMinutes);
	    m_oCreateAfterDate = oCalendar.getTime();			
	}
	
	@Override public void setParameter(final String strParameterName, final String strValue)
	{
		if (strParameterName.equalsIgnoreCase("wait"))
			m_nMinutes = Integer.decode(strValue);
				
		super.setParameter(strParameterName, strValue);
	}
}

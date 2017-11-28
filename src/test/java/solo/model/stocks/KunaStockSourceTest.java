package solo.model.stocks;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.stocks.analyse.IStateAnalysis;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.SimpleStateAnalysis;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.KunaStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.history.StockRateStatesLocalHistory;
import solo.model.stocks.history.StocksHistory;
import solo.model.stocks.item.Event;
import solo.model.stocks.item.EventType;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.oracle.IRateOracle;
import solo.model.stocks.oracle.RateForecast;
import solo.model.stocks.oracle.RatesForecast;
import solo.model.stocks.oracle.SimpleRateOracle;
import solo.model.stocks.source.IStockSource;
import solo.transport.ITransport;
import solo.transport.ITransportMessages;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;
import solo.utils.MathUtils;

public class KunaStockSourceTest
{
    @Test
    public void testKunaStockSource() throws Exception 
    {
    	//	Arrange
    	final IStockExchange oKunaStockExchange = StockExchangeFactory.getStockExchange(KunaStockExchange.NAME);
    	final IStockSource oKunaStockSource = oKunaStockExchange.getStockSource();
    	
    	final IStateAnalysis oStateAnalysis = new SimpleStateAnalysis();
    	final IRateOracle oRateOracle = new SimpleRateOracle();
    	final StockRateStatesLocalHistory oStockRateStatesLocalHistory = new StockRateStatesLocalHistory(100, 1, oRateOracle);
    	
    	//	Act
    	int nCount = 0;
    	final ITransport oTelegram = TransportFactory.getTransport(TelegramTransport.NAME);
    	while(true)
    	{
    		try
    		{
		    	final StockRateStates oStockRateStates = oKunaStockSource.getStockRates();
		    	StocksHistory.addHistory(oKunaStockSource.getStockExchange(), oStockRateStates);

		    	final StateAnalysisResult oStateAnalysisResult = oStateAnalysis.analyse(oStockRateStates, oKunaStockExchange);
	    		oStockRateStatesLocalHistory.addToHistory(oStateAnalysisResult);
		    	
		    	final ITransportMessages oMessages = oTelegram.getMessages();
		    	if (null != oMessages)
	    		{
		    		final String strMessageText = oMessages.getMessages().get(0).getText().replace("_", " ").trim();
		    		if (strMessageText.startsWith("/info "))
		    		{
		    			System.err.printf("Telegram receive command [" + strMessageText + "]\r\n");

		    			final String strRate = strMessageText.substring(6).toUpperCase() + "->UAH";
		    			if (null != oStockRateStates.getRateStates().get(strRate))
		    			{
		    				final RateState oRateState = oStockRateStates.getRateStates().get(strRate);
		    				final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(oRateState.getRateInfo());
		    				String strMessage = "Sell : " + MathUtils.toCurrencyString(oAnalysisResult.getAsksAnalysisResult().getBestPrice(), Currency.UAH) + 
		    									" / " + MathUtils.toCurrencyString(oAnalysisResult.getAsksAnalysisResult().getAverageAllSumPrice(), Currency.UAH) + "\r\n";   
		    				strMessage += "Buy : " + MathUtils.toCurrencyString(oAnalysisResult.getBidsAnalysisResult().getBestPrice(), Currency.UAH) + 
		    									" / " + MathUtils.toCurrencyString(oAnalysisResult.getBidsAnalysisResult().getAverageAllSumPrice(), Currency.UAH) + "\r\n";   
		    				strMessage += "Trades : " + MathUtils.toCurrencyString(oAnalysisResult.getTradesAnalysisResult().getBestPrice(), Currency.UAH) + 
		    									" / " + MathUtils.toCurrencyString(oAnalysisResult.getTradesAnalysisResult().getAverageAllSumPrice(), Currency.UAH) + "\r\n";   
		    				
		    				final List<RatesForecast> oForecasts = oStockRateStatesLocalHistory.getFuture();
		    				if (null != oForecasts && oForecasts.size() > 0)
		    				{
		    					final RatesForecast oForecast = oForecasts.get(0);
		    					final RateForecast oRateForecast = oForecast.getForecust(oRateState.getRateInfo());
		    					strMessage += "Forecast : " + MathUtils.toCurrencyString(oRateForecast.getPrice(), Currency.UAH);
		    				}
		    				oTelegram.sendMessage(strMessage);
		    			}
		    			else
		    				oTelegram.sendMessage("Unknown currency");
		    		}
		    		else if (strMessageText.startsWith("/setEvent "))
		    		{
		    			final String[] aParts = strMessageText.split(" "); 
		    			final Currency oCurrency = Currency.valueOf(aParts[1].toUpperCase());
		    			final RateInfo oRateInfo = new RateInfo(oCurrency, Currency.UAH);
		    			final EventType oEventType = EventType.valueOf(aParts[2].toUpperCase());
		    			final BigDecimal nPrice = MathUtils.fromString(aParts[3]);
		    			
	    				final Event oEvent = new Event(oEventType, oRateInfo, nPrice);
	    				oKunaStockExchange.getEvents().addEvent(oEvent);

	    				System.err.printf("Add event : " + oEvent + "\r\n");
		    		}
		    		else if (strMessageText.startsWith("/deleteEvent "))
		    		{
		    			final String[] aParts = strMessageText.split(" "); 
		    			final Currency oCurrency = Currency.valueOf(aParts[1].toUpperCase());
		    			final EventType oEventType = EventType.valueOf(aParts[2].toUpperCase());
		    			final BigDecimal nPrice = MathUtils.fromString(aParts[3]);

			    		for(final Event oEvent : oKunaStockExchange.getEvents().getList())
			    		{
			    			if (oEvent.getType().equals(oEventType) && oEvent.getRateInfo().getCurrencyFrom().equals(oCurrency) && 
			    					oEvent.getPrice().compareTo(nPrice) == 0)
			    			{
			    				oKunaStockExchange.getEvents().removeEvent(oEvent);
			    				System.err.printf("Remove event : " + oEvent + "\r\n");
			    				break;
			    			}
			    		}
		    		}
		    		else if (strMessageText.startsWith("/getEvents"))
		    		{
		    			String strMessage = StringUtils.EMPTY;
			    		for(final Event oEvent : oKunaStockExchange.getEvents().getList())
			    			strMessage += oEvent.getInfo();

		    			oTelegram.sendMessage((StringUtils.isBlank(strMessage) ? "No events" : strMessage));
		    		}
		    		else
	    				oTelegram.sendMessage("Unknown command");
	    		}
	    		
	    		for(final Event oEvent : oKunaStockExchange.getEvents().getList())
	    		{
	    			if (oEvent.check(oStateAnalysisResult))
	    			{
	    				System.err.printf(oEvent.getMessage());
	    				oTelegram.sendMessage(oEvent.getMessage());
	    			}
	    		}
	    		oKunaStockExchange.getEvents().removeAllOccurred();
		    	
		    	System.err.printf("Count [" + nCount + "]. Date " + (new Date()) + "\r\n");
    		}
    		catch(Exception e) 
    		{
		    	System.err.printf("Count [" + nCount + "]. Exception. [" + e.getMessage() + "]. Date " + (new Date()) + "\r\n");
    		}
	    	nCount++;
    	}

        //	Assert
//    	Assert.assertNotNull(oStockRateStates);
 //   	System.err.printf(oStockRateStates.toString());
    }
}
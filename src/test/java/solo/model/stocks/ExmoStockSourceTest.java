package solo.model.stocks;

import static org.junit.Assert.*;

import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.source.ExmoStockSource;

public class ExmoStockSourceTest
{
    @Test public void initExmoStockSource()
    {
    	//	Arrnage
    	final ExmoStockSource oExmoStockSource = new ExmoStockSource(StockExchangeFactory.getStockExchange(Stocks.Exmo));
    	
    	//	Act
    	oExmoStockSource.init();
    	
    	//	Assert
    }
    
    
    @Test public void getOrderExmoStockSource()
    {
    	//	Arrnage
    	final ExmoStockSource oExmoStockSource = new ExmoStockSource(StockExchangeFactory.getStockExchange(Stocks.Exmo));   	
    	final RateInfo oRateInfo = new RateInfo(Currency.BTC, Currency.EUR);
    	
		//	Act
    	final Order oOrder = oExmoStockSource.getOrder("11813002373", RateInfo.getReverseRate(oRateInfo));
    	
    	//	Assert
    	assertNotNull(oOrder);
    }

}
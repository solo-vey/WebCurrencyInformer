package solo.model.stocks;

import org.junit.Test;

import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
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

}
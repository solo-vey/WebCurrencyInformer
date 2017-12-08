package solo.model.stocks;

import org.junit.Test;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.worker.WorkerFactory;

public class KunaStockSourceTest
{
    @Test public void testKunaStockSource() throws Exception
    {
    	WorkerFactory.start();
    }

    @Test public void testKunaStockSource2() throws Exception
    {
    	IStockExchange oStockExchange = StockExchangeFactory.getStockExchange(Stocks.BtcTrade);
    	StockUserInfo oUserInfo = oStockExchange.getStockSource().getUserInfo(null);

    	System.err.printf(oUserInfo + "\r\n");
    }
    
    
}
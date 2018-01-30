package solo.model.stocks;

import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.worker.WorkerFactory;

public class KunaStockSourceTest
{
    @Test public void testKunaStockSource() throws Exception
    {
    	WorkerFactory.start();
    }
 
    @Test public void test() throws Exception
    {
    	final Order oOrder = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockSource().getOrder("522110737", new RateInfo(Currency.ETH, Currency.RUB));
    	//final Order oOrder = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockSource().getOrder("521665811", new RateInfo(Currency.ETH, Currency.RUB));
    	//final Order oOrder2 = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockSource().getOrder("509446393", new RateInfo(Currency.WAVES, Currency.RUB));
    	
    	System.out.println(oOrder);
    }

}
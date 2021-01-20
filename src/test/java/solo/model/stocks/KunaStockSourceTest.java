package solo.model.stocks;

import java.util.List;

import org.junit.Test;

import solo.CurrencyInformer;
import solo.model.currency.Currency;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.OrderTrade;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.TraceUtils;

public class KunaStockSourceTest
{
    @Test public void testKunaStockSource() throws Exception
    {
    	CurrencyInformer.main(null);
    }
 
    @Test public void test() throws Exception
    {
    	final MainWorker oMainWorker = WorkerFactory.getMainWorker(Stocks.Exmo);
    	WorkerFactory.registerMainWorker(oMainWorker);
    	
    	final IStockExchange oStockExchange = StockExchangeFactory.getStockExchange(Stocks.Exmo);
    	final List<OrderTrade> oOrderTrades = oStockExchange.getStockSource().getTrades("697283058", new RateInfo(Currency.ETH, Currency.UAH));
    	//final Order oOrder = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockSource().getOrder("521665811", new RateInfo(Currency.ETH, Currency.RUB));
    	//final Order oOrder2 = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockSource().getOrder("509446393", new RateInfo(Currency.WAVES, Currency.RUB));
    	
    	TraceUtils.writeTrace(oOrderTrades.toString());
    }

}
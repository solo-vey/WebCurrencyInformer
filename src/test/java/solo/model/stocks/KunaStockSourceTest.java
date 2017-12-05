package solo.model.stocks;

import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.rules.notify.EventBase;
import solo.model.stocks.worker.WorkerFactory;

public class KunaStockSourceTest
{
    @Test public void testKunaStockSource() throws Exception
    {
    	WorkerFactory.start();
    }

    @Test public void testKunaStockSource2() throws Exception
    {
    	IStockExchange oStockExchange = StockExchangeFactory.getStockExchange(Stocks.Mock);
    	final Rules oRules = new Rules(oStockExchange);
    	
    	oRules.addRule(new EventBase(new RateInfo(Currency.BTC, Currency.UAH), "1300"));
    	
    	oRules.save();
    	
    	oRules.load();

    	System.err.printf(oRules + "\r\n");
    }
    
    
}
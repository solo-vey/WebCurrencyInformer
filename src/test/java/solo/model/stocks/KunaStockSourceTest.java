package solo.model.stocks;

import java.io.File;
import java.math.BigDecimal;

import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.TransportFactory;

public class KunaStockSourceTest
{
    @Test public void testKunaStockSource() throws Exception
    {
    	WorkerFactory.start();
    }

    @Test public void test() throws Exception
    {
    	final BigDecimal nMax = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockCandlestick().get(new RateInfo(Currency.ETH, Currency.RUB)).getAverageMaxPrice();
    	final BigDecimal nMin = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockCandlestick().get(new RateInfo(Currency.ETH, Currency.RUB)).getAverageMinPrice();
    	System.err.print(nMax + " - " + nMin);
    }
}
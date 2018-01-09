package solo.model.stocks;

import java.io.File;
import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
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
    	final Candlestick oCandlestick = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockCandlestick().get(new RateInfo(Currency.ETH, Currency.UAH));
    	final String strFileName = oCandlestick.makeChartImage();
    	TransportFactory.getTransport(Stocks.Exmo).sendPhoto(new File(strFileName), "test");
   	}
}
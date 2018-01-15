package solo.model.stocks;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.worker.WorkerFactory;

public class KunaStockSourceTest
{
    @Test public void testKunaStockSource() throws Exception
    {
    	WorkerFactory.start();
    }

    @Test public void test() throws Exception
    {
    	final Candlestick oCandlestick = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockCandlestick().get(new RateInfo(Currency.ETH, Currency.UAH));
    	oCandlestick.makeChartImage();
    	final BigDecimal nMax = oCandlestick.getAverageMaxPrice(3);
    	System.err.print(nMax.toString().replace(",", "\r\n"));
    	
//    	oCandlestick.makeChartImage();
//    	final List<String> aHistoryInfo = oCandlestick.getHistoryInfo();
 //   	System.err.print(aHistoryInfo.toString().replace(",", "\r\n"));
    }
}
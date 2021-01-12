package solo.model.stocks.item.analyse;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import solo.BaseTest;
import solo.model.currency.Currency;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.RateInfo;
import solo.utils.TraceUtils;

public class CandlestickTest extends BaseTest
{
    @Test public void testMakeChartImage() throws IOException
    {
    	//	Arrange
    	final Candlestick oCandlestick = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockCandlestick().get(RateInfo.ETH_UAH);
    	
    	//	Act
    	oCandlestick.makeChartImage(150);
    	
    	//	Asset
    	final File oFile = new File(oCandlestick.getFileName());
    	Assert.assertTrue(oFile.isFile());
    	
    	TraceUtils.writeTrace(oCandlestick.getMax(24).toString());
    }

    @Test public void testCandlestickHistory() throws IOException
    {
    	//	Arrange
    	final Candlestick oCandlestick = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockCandlestick().get(new RateInfo(Currency.ETH, Currency.UAH));
    	
    	oCandlestick.makeChartImage(24);
    	
    	//	Act
    	final List<String> aHistoryInfo = oCandlestick.getHistoryInfo();
    	
    	//	Assert
    	TraceUtils.writeTrace(aHistoryInfo.toString().replace("\r", "").replace("},", "},\r\n"));
    }

    @Test public void testAverageMaxPrice()
    {
    	//	Arrange
    	final Candlestick oCandlestick = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockCandlestick().get(RateInfo.ETH_UAH);
    	
    	//	Act
    	final BigDecimal nMax = oCandlestick.getAverageMaxPrice(3);
    	
    	//	Assert
    	Assert.assertTrue(nMax.compareTo(BigDecimal.ZERO) > 0);
    }
}

package solo.model.stocks.item.analyse;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import solo.BaseTest;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.RateInfo;

public class CandlestickTest extends BaseTest
{
    @Test public void testMakeChartImage() throws Exception
    {
    	//	Arrange
    	final Candlestick oCandlestick = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockCandlestick().get(RateInfo.ETH_UAH);
    	
    	//	Act
    	oCandlestick.makeChartImage(35);
    	
    	//	Asset
    	final File oFile = new File(oCandlestick.getFileName());
    	Assert.assertTrue(oFile.isFile());
    }

    @Test public void testCandlestickHistory() throws Exception
    {
    	//	Arrange
    	final Candlestick oCandlestick = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockCandlestick().get(RateInfo.ETH_UAH);
    	
    	//	Act
    	final List<String> aHistoryInfo = oCandlestick.getHistoryInfo();
    	
    	//	Assert
    	System.err.print(aHistoryInfo.toString().replace("\r", "").replace("},", "},\r\n"));
    }

    @Test public void testAverageMaxPrice() throws Exception
    {
    	//	Arrange
    	final Candlestick oCandlestick = StockExchangeFactory.getStockExchange(Stocks.Exmo).getStockCandlestick().get(RateInfo.ETH_UAH);
    	
    	//	Act
    	final BigDecimal nMax = oCandlestick.getAverageMaxPrice(3);
    	
    	//	Assert
    	Assert.assertTrue(nMax.compareTo(BigDecimal.ZERO) > 0);
    }
}
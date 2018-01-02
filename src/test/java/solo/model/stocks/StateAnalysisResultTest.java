package solo.model.stocks;

import org.junit.Test;

import solo.model.stocks.analyse.IStateAnalysis;
import solo.model.stocks.analyse.SimpleStateAnalysis;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.source.IStockSource;

public class StateAnalysisResultTest
{
    @Test
    public void testStateAnalysisResultConstructor() throws Exception 
    {
    	//	Arrange
    	final IStockExchange oKunaStockExchange = StockExchangeFactory.getStockExchange(Stocks.Kuna);
    	final IStockSource oKunaStockSource = oKunaStockExchange.getStockSource();
    	final StockRateStates oStockRateStates = oKunaStockSource.getStockRates();
    	final IStateAnalysis oStateAnalysis = new SimpleStateAnalysis();
    	
    	//	Act
    	final StateAnalysisResult oStateAnalysisResult = oStateAnalysis.analyse(oStockRateStates, oKunaStockExchange);

        //	Assert
    	System.err.printf(oStockRateStates + "\r\n");
    	System.err.printf(oStateAnalysisResult.toString() + "\r\n");
    }
}
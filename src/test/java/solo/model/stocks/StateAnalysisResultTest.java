package solo.model.stocks;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import solo.model.stocks.analyse.IStateAnalysis;
import solo.model.stocks.analyse.SimpleStateAnalysis;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.history.StockRateStatesLocalHistory;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.oracle.IRateOracle;
import solo.model.stocks.oracle.SimpleRateOracle;
import solo.model.stocks.source.IStockSource;
import solo.model.stocks.source.MockStockSource;

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

    @Test
    public void testStateAnalysisResultOracle() throws Exception 
    {
    	//	Arrange
    	final IStockExchange oMockStockExchange = StockExchangeFactory.getStockExchange(Stocks.Mock);
    	final MockStockSource oMockStockSource = (MockStockSource) oMockStockExchange.getStockSource();
    	final IStateAnalysis oStateAnalysis = new SimpleStateAnalysis();
    	final IRateOracle oRateOracle = new SimpleRateOracle();
    	final StockRateStatesLocalHistory oStockRateStatesLocalHistory = new StockRateStatesLocalHistory(20, 1, oRateOracle);
    	final Calendar oDateStart = Calendar.getInstance();
    	oDateStart.set(2017, 10, 24, 10, 00, 00);
    	oMockStockSource.setDateStart(oDateStart.getTime(), new Date());
    	
    	//	Act
    	for(int i = 0; i < 30; i++)
    	{
    		System.err.printf("Step [" + i + "]\r\n");
        	final StockRateStates oStockRateStates = oMockStockSource.getStockRates();
    		final StateAnalysisResult oStateAnalysisResult = oStateAnalysis.analyse(oStockRateStates, oMockStockExchange);
    		oStockRateStatesLocalHistory.addToHistory(oStateAnalysisResult);
    	}
    	
        //	Assert
    	System.err.printf(oStockRateStatesLocalHistory.getFuture() + "\r\n");
    }
}
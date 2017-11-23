package solo.model.stocks;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class StateAnalysisResultTest
{
    @Test
    public void testStateAnalysisResultConstructor() throws Exception 
    {
    	//	Arrange
    	final IStockExchange oKunaStockExchange = StockExchangeFactory.getStockExchange(KunaStockExchange.NAME);
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
    	final IStockExchange oMockStockExchange = StockExchangeFactory.getStockExchange(MockStockExchange.NAME);
    	final MockStockSource oMockStockSource = (MockStockSource) oMockStockExchange.getStockSource();
    	final IStateAnalysis oStateAnalysis = new SimpleStateAnalysis();
    	final IRateOracle oRateOracle = new SimpleRateOracle();
    	final StockRateStatesLocalHistory oStockRateStatesLocalHistory = new StockRateStatesLocalHistory(100, 10, oRateOracle);
    	final Calendar oDateStart = Calendar.getInstance();
    	oDateStart.set(2017, 10, 22, 19, 24, 00);
    	oMockStockSource.setDateStart(oDateStart.getTime(), new Date());
    	
    	//	Act
    	for(int i = 0; i < 200; i++)
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
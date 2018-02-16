package solo.model.stocks.item.rules.task.manager;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.trade.TradeInfo;
import solo.model.stocks.worker.MainWorker;
import solo.model.stocks.worker.WorkerFactory;

public class PeriodTradesBlockTest
{
    @Test public void testAddTradeRotate() throws Exception
    {
    	//	Arrange
    	WorkerFactory.registerMainWorkerThread(Thread.currentThread().getId(), new MainWorker(Stocks.Mock));
    	
    	final PeriodTradesBlock oPeriodTradesBlock = new PeriodTradesBlock();
    	final RateInfo oRateInfo = new RateInfo(Currency.BTC, Currency.UAH);
    	for(int nPos = 1; nPos <= 24; nPos++)
    	{
    		final TradeInfo oTradeInfo = new TradeInfo(oRateInfo, 0);
    		oTradeInfo.addBuy(new BigDecimal(nPos), new BigDecimal(nPos));
    		oPeriodTradesBlock.addTrade(nPos, oTradeInfo);
    	}
    	
    	for(int nPos = 1; nPos < 15; nPos++)
    	{
    		final TradeInfo oTradeInfo = new TradeInfo(oRateInfo, 0);
    		oTradeInfo.addBuy(new BigDecimal(nPos), new BigDecimal(nPos));
    		oPeriodTradesBlock.addTrade(nPos, oTradeInfo);
    	}
    	
    	//	Act
		final TradeInfo oTradeInfo = new TradeInfo(oRateInfo, 0);
		oTradeInfo.addBuy(new BigDecimal(15), new BigDecimal(15));
		oPeriodTradesBlock.addTrade(15, oTradeInfo);
    	
    	//	Assert
    	Assert.assertEquals(24, oPeriodTradesBlock.m_oPeriodTrades.size());
    	
    }
	
}

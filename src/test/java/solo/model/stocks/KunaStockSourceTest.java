package solo.model.stocks;

import org.junit.Test;

import solo.model.currency.Currency;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.exchange.Stocks;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;

public class KunaStockSourceTest
{
    @Test public void testKunaStockSource() throws Exception
    {
    	WorkerFactory.start();
    }

    @Test public void testKunaStockSource2() throws Exception
    {
    	final RateInfo oRateInfo = new RateInfo(Currency.ETH, Currency.UAH);
    	final IStockExchange oStockExchange = StockExchangeFactory.getStockExchange(Stocks.BtcTrade);
    	final StockUserInfo oUserInfo = oStockExchange.getStockSource().getUserInfo(null);
    	
    	final Order oAddOrder = oStockExchange.getStockSource().addOrder(OrderSide.BUY, oRateInfo, MathUtils.getBigDecimal(0.01, 6), MathUtils.getBigDecimal(10000, 0));
//    	final Order oRemoveOrder = oStockExchange.getStockSource().removeOrder(oUserInfo.getOrders(oRateInfo).get(0).getId());

    	System.err.printf(oAddOrder + "\r\n");
//    	System.err.printf(oRemoveOrder + "\r\n");
    }
    
    
}
package solo.model.stocks.source;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.item.StockUserInfo;

public interface IStockSource
{
	StockRateStates getStockRates() throws Exception;
	IStockExchange getStockExchange();
	List<RateInfo> getRates();
	Order getOrder(String strOrderId, final RateInfo oRateInfo);
	Order removeOrder(String strOrderId);
	Order addOrder(OrderSide oSite, RateInfo oRateInfo, BigDecimal nVolume, BigDecimal nPrice);
	StockUserInfo getUserInfo(final RateInfo oRateInfo);
	void restart();
}

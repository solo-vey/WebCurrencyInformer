package solo.model.stocks.source;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.StockUserInfo;

public interface IStockSource
{
	RateState getRateState(RateInfo oRateInfo) throws Exception;
	IStockExchange getStockExchange();
	List<RateInfo> getRates();
	List<RateInfo> getAllRates();
	void registerRate(final RateInfo oRateInfo) throws Exception;
	void removeRate(final RateInfo oRateInfo);
	Order getOrder(String strOrderId, final RateInfo oRateInfo);
	Order removeOrder(String strOrderId);
	Order addOrder(OrderSide oSite, RateInfo oRateInfo, BigDecimal nVolume, BigDecimal nPrice);
	StockUserInfo getUserInfo(final RateInfo oRateInfo) throws Exception;
	void restart();
	List<Order> getTrades(RateInfo m_oRateInfo, final int nPage, final int nCount);
}

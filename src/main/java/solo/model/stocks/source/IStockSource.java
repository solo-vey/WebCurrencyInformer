package solo.model.stocks.source;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateParamters;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.StockUserInfo;

public interface IStockSource
{
	void init();
	RateState getRateState(final RateInfo oRateInfo) throws Exception;
	RateState getCachedRateState(final RateInfo oRateInfo) throws Exception;
	Map<RateInfo, RateStateShort> getAllRateState() throws Exception;
	IStockExchange getStockExchange();
	List<RateInfo> getRates();
	Collection<RateInfo> getAllRates();
	void registerRate(final RateInfo oRateInfo) throws Exception;
	void removeRate(final RateInfo oRateInfo);
	Order getOrder(String strOrderId, final RateInfo oRateInfo);
	Order removeOrder(String strOrderId);
	Order addOrder(OrderSide oSite, RateInfo oRateInfo, BigDecimal nVolume, BigDecimal nPrice);
	StockUserInfo getUserInfo(final RateInfo oRateInfo) throws Exception;
	List<Order> getTrades(RateInfo m_oRateInfo, final int nPage, final int nCount);
	RateParamters getRateParameters(final RateInfo oRateInfo);
}

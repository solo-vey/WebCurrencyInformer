package solo.model.stocks.source;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockRateStates;
import solo.model.stocks.item.StockUserInfo;

public interface IStockSource
{
	StockRateStates getStockRates() throws Exception;
	IStockExchange getStockExchange();
	List<RateInfo> getRates();
	Order removeOrder(String strOrderId) throws Exception;
	Order addOrder(String strSite, RateInfo oRateInfo, BigDecimal nVolume, BigDecimal nPrice) throws Exception;
	StockUserInfo getUserInfo(final RateInfo oRateInfo) throws Exception;
	void restart();
}

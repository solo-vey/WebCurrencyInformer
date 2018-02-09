package solo.model.stocks.item.rules.task.strategy.trade;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.rules.task.trade.TradeInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;

public class CurrentPriceBuyStrategy extends BaseStrategy implements IBuyStrategy
{
	private static final long serialVersionUID = -4917813447504424168L;
	
	public final static String NAME = "CurrentPriceBuy";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getBuyPrice(final RateAnalysisResult oRateAnalysisResult, final TradeInfo oTradeInfo)
	{
		final List<Order> oBids = oRateAnalysisResult.getBidsOrders();
		final BigDecimal oMinChangePrice = TradeUtils.getMinChangePrice();
		for(final Order oBidOrder : oBids)
		{
			if (oBidOrder.getPrice().compareTo(oTradeInfo.getCriticalPrice()) < 0)
				return oBidOrder.getPrice().add(oMinChangePrice);
		}
		
		return oTradeInfo.getCriticalPrice();
	}

}

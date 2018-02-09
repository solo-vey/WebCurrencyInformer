package solo.model.stocks.item.rules.task.strategy.trade;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.rules.task.trade.TradeInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;

public class CurrentPriceSellStrategy extends BaseStrategy implements ISellStrategy
{
	private static final long serialVersionUID = -4917813557504424168L;
	
	public final static String NAME = "CurrentPriceSell";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getSellPrice(final RateAnalysisResult oRateAnalysisResult, final TradeInfo oTradeInfo)
	{
		final List<Order> oAsks = oRateAnalysisResult.getAsksOrders();
		final BigDecimal oMinChangePrice = TradeUtils.getMinChangePrice();
		for(final Order oAskOrder : oAsks)
		{
			if (oAskOrder.getPrice().compareTo(oTradeInfo.getCriticalPrice()) > 0)
				return oAskOrder.getPrice().add(oMinChangePrice.negate());
		}
		
		return oTradeInfo.getCriticalPrice();
	}

}

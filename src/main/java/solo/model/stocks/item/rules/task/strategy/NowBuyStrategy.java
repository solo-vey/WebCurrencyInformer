package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.rules.task.trade.TradeInfo;

public class NowBuyStrategy extends BaseStrategy implements IBuyStrategy
{
	private static final long serialVersionUID = -4917816147504424168L;
	
	public static final String NAME = "NowBuy";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getBuyPrice(final RateAnalysisResult oRateAnalysisResult, final TradeInfo oTradeInfo)
	{
		final List<Order> oAsks = oRateAnalysisResult.getAsksOrders();
		BigDecimal nFullSumPrice = oAsks.get(0).getPrice();
		BigDecimal nFullVolume = oTradeInfo.getNeedBoughtVolume();
		int nOrderPosition = 0;
		while (BigDecimal.ZERO.compareTo(nFullVolume) > 0 && nOrderPosition < oAsks.size())
		{
			nFullVolume = nFullVolume.add(oAsks.get(nOrderPosition).getVolume().negate()); 
			nFullSumPrice = oAsks.get(nOrderPosition).getPrice();
			nOrderPosition++;
		}
		return nFullSumPrice;
	}

}

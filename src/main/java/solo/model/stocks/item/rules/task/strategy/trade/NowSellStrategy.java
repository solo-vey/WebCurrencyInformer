package solo.model.stocks.item.rules.task.strategy.trade;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.rules.task.trade.TradeInfo;

public class NowSellStrategy extends BaseStrategy implements ISellStrategy
{
	private static final long serialVersionUID = -4917816147504424168L;
	
	public final static String NAME = "NowBuy";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getSellPrice(final RateAnalysisResult oRateAnalysisResult, final TradeInfo oTradeInfo)
	{
		final List<Order> oBids = oRateAnalysisResult.getBidsOrders();
		BigDecimal nFullSumPrice = oBids.get(0).getPrice();
		BigDecimal nFullVolume = oTradeInfo.getNeedSellVolume();
		int nOrderPosition = 0;
		while (nFullVolume.compareTo(BigDecimal.ZERO) > 0 && nOrderPosition < oBids.size())
		{
			nFullVolume = nFullVolume.add(oBids.get(nOrderPosition).getVolume().negate()); 
			nFullSumPrice = oBids.get(nOrderPosition).getPrice();
			nOrderPosition++;
		}
		return nFullSumPrice;
	}

}

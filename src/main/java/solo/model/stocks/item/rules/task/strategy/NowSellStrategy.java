package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;

public class NowSellStrategy extends BaseStrategy implements ISellStrategy
{
	private static final long serialVersionUID = -4917816147504424168L;
	
	public final static String NAME = "NowBuy";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getSellPrice(final RateAnalysisResult oRateAnalysisResult)
	{
		final List<Order> oBids = oRateAnalysisResult.getBidsOrders();
		BigDecimal nFullSumPrice = oBids.get(0).getPrice();
		BigDecimal nFullSum = BigDecimal.ZERO; // ???  *1.1
		int nOrderPosition = 0;
		while (nFullSum.compareTo(BigDecimal.ZERO) > 0 && nOrderPosition < oBids.size())
		{
			nFullSum = nFullSum.add(oBids.get(nOrderPosition).getPrice().negate()); 
			nFullSumPrice = oBids.get(nOrderPosition).getPrice();
			nOrderPosition++;
		}
		return nFullSumPrice;
	}

}

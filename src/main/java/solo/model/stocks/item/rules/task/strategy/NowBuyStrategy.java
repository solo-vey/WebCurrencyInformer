package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;

public class NowBuyStrategy extends BaseStrategy implements IBuyStrategy
{
	private static final long serialVersionUID = -4917816147504424168L;
	
	public final static String NAME = "NowBuy";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getBuyPrice(final RateAnalysisResult oRateAnalysisResult)
	{
		final List<Order> oAsks = oRateAnalysisResult.getAsksOrders();
		BigDecimal nFullSumPrice = oAsks.get(0).getPrice();
		BigDecimal nFullSum = BigDecimal.ZERO; // ???  *1.1
		int nOrderPosition = 0;
		while (nFullSum.compareTo(BigDecimal.ZERO) > 0 && nOrderPosition < oAsks.size())
		{
			nFullSum = nFullSum.add(oAsks.get(nOrderPosition).getPrice().negate()); 
			nFullSumPrice = oAsks.get(nOrderPosition).getPrice();
			nOrderPosition++;
		}
		return nFullSumPrice;
	}

}

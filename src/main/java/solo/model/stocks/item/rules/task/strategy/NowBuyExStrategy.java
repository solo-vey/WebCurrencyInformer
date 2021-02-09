package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.rules.task.trade.TestTradeInfo;
import solo.model.stocks.item.rules.task.trade.TradeInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.utils.MathUtils;
import solo.utils.TraceUtils;

public class NowBuyExStrategy extends BaseStrategy implements IBuyStrategy
{
	private static final long serialVersionUID = -4917816147504424169L;
	
	public static final String NAME = "NowBuyEx";
	
	public String getName()
	{
		return NAME;
	}
	
	public BigDecimal getBuyPrice(final RateAnalysisResult oRateAnalysisResult, final TradeInfo oTradeInfo)
	{
		List<Order> oBids = oRateAnalysisResult.getBidsOrders();
		final BigDecimal oBestPrice = oBids.get(0).getPrice();
		final List<Order> oMyOrders = TradeUtils.getMyOrders();
		oBids = StrategyUtils.removeMyOrders(oBids, oMyOrders);
		final BigDecimal oPrice = oBids.get(0).getPrice().add(TradeUtils.getMinChangePrice());
		final boolean bIsTest = (oTradeInfo instanceof TestTradeInfo);
		if (!bIsTest)
			TraceUtils.writeError("NowBuyEx [" + oRateAnalysisResult.getRateInfo() + "] " + MathUtils.toCurrencyString(oPrice) + " / " + MathUtils.toCurrencyString(oPrice) + 
				" / " + MathUtils.toCurrencyString(oPrice.add(oBestPrice.negate())));
		return oPrice;
	}
}

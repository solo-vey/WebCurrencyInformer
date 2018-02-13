package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.item.rules.task.trade.TradeInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.utils.MathUtils;

public class HalfSellStrategy extends QuickSellStrategy
{
	private static final long serialVersionUID = -4917817147554424168L;
	
	public final static String NAME = "HalfSell";
	
	public String getName()
	{
		return NAME;
	}

	public BigDecimal getSellPrice(final RateAnalysisResult oRateAnalysisResult, final TradeInfo oTradeInfo)
	{
		final BigDecimal nBestPrice = getBestPrice(oRateAnalysisResult, oTradeInfo);
		final BigDecimal nNowSellPrice = StrategyFactory.getSellStrategy(NowSellStrategy.NAME).getSellPrice(oRateAnalysisResult, oTradeInfo);
		final BigDecimal nHalfSellPrice = MathUtils.getBigDecimal((nBestPrice.doubleValue() + nNowSellPrice.doubleValue()) / 2, TradeUtils.DEFAULT_PRICE_PRECISION); 
		return oTradeInfo.trimSellPrice(nHalfSellPrice);
	}
}

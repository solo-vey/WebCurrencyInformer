package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.rules.task.strategy.trade.DropSellTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.ITradeStrategy;
import solo.model.stocks.item.rules.task.strategy.IBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.ISellStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickSellStrategy;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.source.IStockSource;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;
import solo.utils.MathUtils;
import solo.utils.ResourceUtils;

public class TradeUtils
{
	public static final int DEFAULT_VOLUME_PRECISION = 8;
	public static final int DEFAULT_PRICE_PRECISION = 8;

	public static BigDecimal getStockCommision()
	{
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final BigDecimal nStockCommision = new BigDecimal(ResourceUtils.getIntFromResource("stock.commision", oStockExchange.getStockProperties(), 25));
		return nStockCommision.divide(new BigDecimal(10000));
	}

	public static BigDecimal getCommisionValue(final BigDecimal nValue)
	{
		final BigDecimal nCommision = getStockCommision();
		return nValue.multiply(nCommision);
	}

	public static BigDecimal getWithoutCommision(final BigDecimal nValue)
	{
		final BigDecimal nCommision = getStockCommision();
		return nValue.add(nValue.multiply(nCommision).negate());
	}

	public static BigDecimal getCommisionValue(final BigDecimal nBuyPrice, final BigDecimal nSellPrice)
	{
		final BigDecimal nCommision = getStockCommision();
		return nBuyPrice.multiply(nCommision).add(nSellPrice.multiply(nCommision));
	}

	public static String getMarket(final RateInfo oRateInfo)
	{
		return oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + oRateInfo.getCurrencyTo().toString().toLowerCase();
	}
	
	public static BigDecimal getMarginValue(final BigDecimal nPrice, final RateInfo oRateInfo)
	{
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strMarket = getMarket(oRateInfo); 
		final BigDecimal nStockMarketMargin = new BigDecimal(ResourceUtils.getIntFromResource("stock." + strMarket + ".margin", oStockExchange.getStockProperties(), Integer.MIN_VALUE));
		if (nStockMarketMargin.compareTo(BigDecimal.ZERO) >= 0)
		{
			final BigDecimal nMarketMargin = nStockMarketMargin.divide(new BigDecimal(10000));
			return nPrice.multiply(nMarketMargin);			
		}
		
		final BigDecimal nStockMargin = new BigDecimal(ResourceUtils.getIntFromResource("stock.margin", oStockExchange.getStockProperties(), 20));
		final BigDecimal nMargin = nStockMargin.divide(new BigDecimal(10000));
		return nPrice.multiply(nMargin);
	}
	
	public static int getPricePrecision(final RateInfo oRateInfo)
	{
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		
		final String strMarket = getMarket(oRateInfo); 
		final int nMarketPricePrecision = ResourceUtils.getIntFromResource("stock." + strMarket + ".price.precision", oStockExchange.getStockProperties(), Integer.MAX_VALUE);
		if (nMarketPricePrecision != Integer.MAX_VALUE)
			return nMarketPricePrecision;
		
		return ResourceUtils.getIntFromResource("stock.price.precision", oStockExchange.getStockProperties(), DEFAULT_PRICE_PRECISION);
	}
	
	public static int getVolumePrecision(final RateInfo oRateInfo)
	{
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		
		final String strMarket = getMarket(oRateInfo); 
		final int nMarketVolumePrecision = ResourceUtils.getIntFromResource("stock." + strMarket + ".volume.precision", oStockExchange.getStockProperties(), Integer.MAX_VALUE);
		if (nMarketVolumePrecision != Integer.MAX_VALUE)
			return nMarketVolumePrecision;
		
		return ResourceUtils.getIntFromResource("stock.volume.precision", oStockExchange.getStockProperties(), DEFAULT_VOLUME_PRECISION);
	}
	
	public static int getFakeMinPrice(final RateInfo oRateInfo)
	{
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		
		final String strMarket = getMarket(oRateInfo); 
		final int nMarketFakeMinPrice = ResourceUtils.getIntFromResource("stock." + strMarket + ".fake_price", oStockExchange.getStockProperties(), Integer.MAX_VALUE);
		if (nMarketFakeMinPrice != Integer.MAX_VALUE)
			return nMarketFakeMinPrice;
		
		return ResourceUtils.getIntFromResource("stock.fake_price", oStockExchange.getStockProperties(), 500);
	}
	
	public static BigDecimal getMinChangePrice()
	{
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strMinDelta = ResourceUtils.getResource("stock.min_change_price", oStockExchange.getStockProperties(), "1");
		return MathUtils.fromString(strMinDelta);
	}
	
	public static BigDecimal getRoundedPrice(final RateInfo oRateInfo, final BigDecimal nPrice)
	{
		return MathUtils.getBigDecimal(nPrice, getPricePrecision(oRateInfo));
	}
	
	public static BigDecimal getRoundedVolume(final RateInfo oRateInfo, final BigDecimal nVolume)
	{
		return MathUtils.getBigDecimal(nVolume, getVolumePrecision(oRateInfo));
	}

	public static BigDecimal getMinTradeVolume(final RateInfo oOriginalRateInfo)
	{
		final RateInfo oRateInfo = (oOriginalRateInfo.getIsReverse() ? RateInfo.getReverseRate(oOriginalRateInfo) : oOriginalRateInfo);
		
		BigDecimal nMinTradeVolume = WorkerFactory.getStockSource().getRateParameters(oRateInfo).getMinQuantity();
		if (nMinTradeVolume.compareTo(BigDecimal.ZERO) == 0)
		{
			final String strMarket = getMarket(oRateInfo); 
			final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
			String strMinVolume = ResourceUtils.getResource("stock." + strMarket + ".min_volume", oStockExchange.getStockProperties(), StringUtils.EMPTY);
			if (StringUtils.isBlank(strMinVolume))
			{	
				final String strCurrency = oRateInfo.getCurrencyFrom().toString().toLowerCase();
				strMinVolume = ResourceUtils.getResource("stock." + strCurrency + ".min_volume", oStockExchange.getStockProperties(), "0.000001");
			}
			nMinTradeVolume = MathUtils.getBigDecimal(Double.parseDouble(strMinVolume), TradeUtils.getVolumePrecision(oRateInfo));
		}
		
		if (!oOriginalRateInfo.getIsReverse())
			return TradeUtils.getRoundedPrice(oOriginalRateInfo, nMinTradeVolume);
		
		final StateAnalysisResult oStateAnalysisResult = WorkerFactory.getMainWorker().getStockExchange().getLastAnalysisResult();
		final RateAnalysisResult oRateAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(oRateInfo);
		final BigDecimal nSellPrice = oRateAnalysisResult.getAsksOrders().get(0).getPrice();
		return TradeUtils.getRoundedPrice(oOriginalRateInfo, nMinTradeVolume.multiply(nSellPrice));
	}
	
	public static BigDecimal getMinTradeSum(final RateInfo oOriginalRateInfo)
	{
		final StateAnalysisResult oStateAnalysisResult = WorkerFactory.getMainWorker().getStockExchange().getLastAnalysisResult();
		final BigDecimal oMinTradeVolume = TradeUtils.getMinTradeVolume(oOriginalRateInfo);
		final BigDecimal oBuyPrice = oStateAnalysisResult.getRateAnalysisResult(oOriginalRateInfo).getBidsOrders().get(0).getPrice();
		final BigDecimal nMinTradeSum = oMinTradeVolume.multiply(oBuyPrice).multiply(new BigDecimal(1.01));
				
		final RateInfo oRateInfo = (oOriginalRateInfo.getIsReverse() ? RateInfo.getReverseRate(oOriginalRateInfo) : oOriginalRateInfo);
		final BigDecimal nStockMinTradeSum = (!oOriginalRateInfo.getIsReverse() ? WorkerFactory.getStockSource().getRateParameters(oRateInfo).getMinAmount()
												: WorkerFactory.getStockSource().getRateParameters(oRateInfo).getMinQuantity());
		return (nStockMinTradeSum.compareTo(BigDecimal.ZERO) > 0 && nStockMinTradeSum.compareTo(nMinTradeSum) > 0 ? nStockMinTradeSum : nMinTradeSum);
	}

	public static IBuyStrategy getBuyStrategy(final RateInfo oRateInfo)
	{
		String strMarket = getMarket(oRateInfo); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		String strBuyStrategy = ResourceUtils.getResource("stock." + strMarket + ".buy_strategy", oStockExchange.getStockProperties(), StringUtils.EMPTY);
		if (StringUtils.isBlank(strBuyStrategy))
		{
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo);
			strMarket = getMarket(oReverseRateInfo);
			strBuyStrategy = ResourceUtils.getResource("stock." + strMarket + ".buy_strategy", oStockExchange.getStockProperties(), StringUtils.EMPTY);
		}
		if (StringUtils.isBlank(strBuyStrategy))
			strBuyStrategy = ResourceUtils.getResource("stock.buy_strategy", oStockExchange.getStockProperties(), QuickBuyStrategy.NAME);
		
		return StrategyFactory.getBuyStrategy(strBuyStrategy);
	}

	public static ISellStrategy getSellStrategy(final RateInfo oRateInfo)
	{
		final String strMarket = getMarket(oRateInfo); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strSellStrategy = ResourceUtils.getResource("stock." + strMarket + ".sell_strategy", oStockExchange.getStockProperties(), QuickSellStrategy.NAME);
		return StrategyFactory.getSellStrategy(strSellStrategy);
	}

	public static ITradeStrategy getTradeStrategy(final RateInfo oRateInfo)
	{
		final String strMarket = getMarket(oRateInfo); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strTradeStrategy = ResourceUtils.getResource("stock." + strMarket + ".trade_strategy", oStockExchange.getStockProperties(), DropSellTradeStrategy.NAME);
		return StrategyFactory.getTradeStrategy(strTradeStrategy);
	}
	
	public static List<Order> getMyOrders()
	{
		final List<Order> oMyOrders = new LinkedList<Order>();
		final Rules oStockRules = WorkerFactory.getMainWorker().getStockExchange().getRules();
		for(final IRule oRule : oStockRules.getRules().values())
		{
			final ITradeTask oTradeTask = getRuleAsTradeTask(oRule);
			if (null == oTradeTask)
				continue;
			
			final Order oOrder = oTradeTask.getTradeInfo().getOrder();
			if (!oOrder.isNull())
				oMyOrders.add(oOrder);
		}
		
		return oMyOrders;
	}
	
	public static ITradeTask getRuleAsTradeTask(final IRule oRule)
	{
		if (oRule instanceof ITradeTask)
			return (ITradeTask)oRule;

		return null;
	}
	
	public static ITradeControler getRuleAsTradeControler(final IRule oRule)
	{
		if (oRule instanceof ITradeControler)
			return (ITradeControler)oRule;
		
		return null;
	}

	public static Order makeReveseOrder(final Order oOrder)
	{
		final Order oReverseOrder = new Order();
		oReverseOrder.setCreated(oOrder.getCreated());
		oReverseOrder.setId(oOrder.getId());
		oReverseOrder.setState(oOrder.getState());
		oReverseOrder.setVolume(oOrder.getSum());
		oReverseOrder.setPrice(MathUtils.getBigDecimal(1.0 / oOrder.getPrice().doubleValue(), 16));
		if (null != oOrder.getSide())
			oReverseOrder.setSide(oOrder.getSide().equals(OrderSide.SELL) ? OrderSide.BUY : OrderSide.SELL);

		return oReverseOrder;
	}
	
	public static Order removeOrder(final Order oGetOrder, final RateInfo oRateInfo, final IStockSource oStockSource)
	{
		int nTryCount = 50;
		final String strMessage = "Cannot delete order\r\n" + oGetOrder.getInfoShort();
		Order oRemoveOrder = new Order(Order.ERROR, strMessage);
		while (nTryCount > 0)
		{
			oRemoveOrder = oStockSource.removeOrder(oGetOrder.getId(), oRateInfo);
			if (oRemoveOrder.isCanceled())
				return oRemoveOrder;

			if (!oRemoveOrder.isException())
			{
				final Order oCheckRemoveOrder = oStockSource.getOrder(oGetOrder.getId(), oRateInfo);
				if (oCheckRemoveOrder.isDone() || oCheckRemoveOrder.isCanceled())
					return oCheckRemoveOrder;
			}
			
			try { Thread.sleep(100); }
			catch (InterruptedException e) { break; }
			nTryCount--;
		}

		WorkerFactory.getMainWorker().sendMessage(MessageLevel.ERROR, strMessage);
		return oRemoveOrder;
	}
	
	public static boolean isVerySmallSum(final RateInfo oRateInfo, final BigDecimal nSum)
	{
		final BigDecimal nRoundedSum = TradeUtils.getRoundedPrice(oRateInfo, nSum);
		return (nRoundedSum.compareTo(BigDecimal.ZERO) == 0);
	}
	
	public static boolean isVerySmallVolume(final RateInfo oRateInfo, final BigDecimal nVolume)
	{
		final BigDecimal nRoundedVolume = TradeUtils.getRoundedVolume(oRateInfo, nVolume);
		return (nRoundedVolume.compareTo(BigDecimal.ZERO) == 0);
	}
}


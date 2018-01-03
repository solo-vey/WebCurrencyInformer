package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.rules.task.TaskBase;
import solo.model.stocks.item.rules.task.TaskFactory;
import solo.model.stocks.item.rules.task.strategy.IBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.ISellStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickSellStrategy;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
import ua.lz.ep.utils.ResourceUtils;

public class TradeUtils
{
	public static final int DEFAULT_VOLUME_PRECISION = 6;
	public static final int DEFAULT_PRICE_PRECISION = 0;

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

	public static BigDecimal getMarginValue(final BigDecimal nPrice)
	{
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final BigDecimal nStockCommision = new BigDecimal(ResourceUtils.getIntFromResource("stock.margin", oStockExchange.getStockProperties(), 20));
		final BigDecimal nCommision = nStockCommision.divide(new BigDecimal(10000));
		return nPrice.multiply(nCommision);
	}
	
	public static int getPricePrecision(final RateInfo oRateInfo)
	{
		final String strMarket = oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + oRateInfo.getCurrencyTo().toString().toLowerCase(); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		return ResourceUtils.getIntFromResource("stock." + strMarket + ".price.precision", oStockExchange.getStockProperties(), DEFAULT_PRICE_PRECISION);
	}
	
	public static int getVolumePrecision(final RateInfo oRateInfo)
	{
		final String strMarket = oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + oRateInfo.getCurrencyTo().toString().toLowerCase(); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		return ResourceUtils.getIntFromResource("stock." + strMarket + ".volume.precision", oStockExchange.getStockProperties(), DEFAULT_VOLUME_PRECISION);
	}
	
	public static int getFakeMinPrice(final RateInfo oRateInfo)
	{
		final String strMarket = oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + oRateInfo.getCurrencyTo().toString().toLowerCase(); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		return ResourceUtils.getIntFromResource("stock." + strMarket + ".fake_price", oStockExchange.getStockProperties(), 500);
	}
	
	public static BigDecimal getMinChangePrice()
	{
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strMinDelta = ResourceUtils.getResource("stock.min_change_price", oStockExchange.getStockProperties(), "1");
		return MathUtils.fromString(strMinDelta);
	}
	
	public static BigDecimal getRoundedPrice(final RateInfo oRateInfo, final BigDecimal nPrice)
	{
		return MathUtils.getBigDecimal(nPrice.doubleValue(), getPricePrecision(oRateInfo));
	}
	
	public static BigDecimal getRoundedCriticalPrice(final RateInfo oRateInfo, final BigDecimal nPrice)
	{
		return MathUtils.getBigDecimalRoundedUp(nPrice.doubleValue(), getPricePrecision(oRateInfo));
	}
	
	public static BigDecimal getRoundedVolume(final RateInfo oRateInfo, final BigDecimal nVolume)
	{
		return MathUtils.getBigDecimal(nVolume.doubleValue(), getVolumePrecision(oRateInfo));
	}

	public static BigDecimal getMinTradeVolume(final RateInfo oRateInfo)
	{
		final String strMarket = oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + oRateInfo.getCurrencyTo().toString().toLowerCase(); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strMinVolume = ResourceUtils.getResource("stock." + strMarket + ".min_volume", oStockExchange.getStockProperties(), "0.000001");
		final BigDecimal nMinTradeVolume = MathUtils.getBigDecimal(Double.parseDouble(strMinVolume), TradeUtils.getVolumePrecision(oRateInfo));
		return nMinTradeVolume;
	}

	public static IBuyStrategy getBuyStrategy(final RateInfo oRateInfo)
	{
		final String strMarket = oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + oRateInfo.getCurrencyTo().toString().toLowerCase(); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strBuyStrategy = ResourceUtils.getResource("stock." + strMarket + ".buy_strategy", oStockExchange.getStockProperties(), QuickBuyStrategy.NAME);
		return StrategyFactory.getBuyStrategy(strBuyStrategy);
	}

	public static ISellStrategy getSellStrategy(final RateInfo oRateInfo)
	{
		final String strMarket = oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + oRateInfo.getCurrencyTo().toString().toLowerCase(); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strSellStrategy = ResourceUtils.getResource("stock." + strMarket + ".sell_strategy", oStockExchange.getStockProperties(), QuickSellStrategy.NAME);
		return StrategyFactory.getSellStrategy(strSellStrategy);
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

		if (!(oRule instanceof TaskFactory))
			return null;
			
		final TaskBase oTask = ((TaskFactory)oRule).getTaskBase();
		if (oTask instanceof ITradeTask)
			return (ITradeTask)oTask;
		
		return null;
	}
	
	public static ITradeControler getRuleAsTradeControler(final IRule oRule)
	{
		if (!(oRule instanceof TaskFactory))
			return null;
			
		final TaskBase oTask = ((TaskFactory)oRule).getTaskBase();
		if (oTask instanceof ITradeControler)
			return (ITradeControler)oTask;
		
		return null;
	}
}


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
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
import ua.lz.ep.utils.ResourceUtils;

public class TradeUtils
{
	public static final int DEFAULT_VOLUME_PRECISION = 6;

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
		return ResourceUtils.getIntFromResource("stock." + strMarket + ".price.precision", oStockExchange.getStockProperties(), 0);
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
	
	public static List<Order> getMyOrders()
	{
		final List<Order> oMyOrders = new LinkedList<Order>();
		final Rules oStockRules = WorkerFactory.getMainWorker().getStockExchange().getRules();
		for(final IRule oRule : oStockRules.getRules().values())
		{
			if (!(oRule instanceof TaskFactory))
				continue;
					
			final TaskBase oTask = ((TaskFactory)oRule).getTaskBase();
			if (!(oTask instanceof ITradeTask))
				continue;
			
			final Order oOrder = ((ITradeTask)oTask).getTradeInfo().getOrder();
			if (!oOrder.isNull())
				oMyOrders.add(oOrder);
		}
		
		return oMyOrders;
	}

}


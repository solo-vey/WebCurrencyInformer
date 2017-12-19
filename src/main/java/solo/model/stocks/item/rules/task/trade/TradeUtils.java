package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
import ua.lz.ep.utils.ResourceUtils;

public class TradeUtils
{
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
		return ResourceUtils.getIntFromResource("stock." + strMarket + ".precision", oStockExchange.getStockProperties(), 0);
	}
	
	public static int getVolumePrecision(final RateInfo oRateInfo)
	{
		final String strMarket = oRateInfo.getCurrencyFrom().toString().toLowerCase() + "_" + oRateInfo.getCurrencyTo().toString().toLowerCase(); 
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		return ResourceUtils.getIntFromResource("stock." + strMarket + ".precision", oStockExchange.getStockProperties(), 6);
	}
	
	public static BigDecimal getRoundedPrice(final RateInfo oRateInfo, final BigDecimal nPrice)
	{
		return MathUtils.getBigDecimal(nPrice.doubleValue(), getPricePrecision(oRateInfo));
	}
	
	public static BigDecimal getRoundedVolume(final RateInfo oRateInfo, final BigDecimal nVolume)
	{
		return MathUtils.getBigDecimal(nVolume.doubleValue(), getVolumePrecision(oRateInfo));
	}
}


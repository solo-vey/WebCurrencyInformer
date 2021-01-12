package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.trade.TradeUtils;

public class StrategyUtils
{
	public static boolean isDeltaTooSmall(List<Order> oAsks, List<Order> oBids, final RateInfo oRateInfo)
	{
		final BigDecimal nAskPrice = getBestPrice(oAsks);
		final BigDecimal nBidPrice = getBestPrice(oBids);
		BigDecimal nDelta = nAskPrice.add(nBidPrice.negate());
		if (nDelta.compareTo(BigDecimal.ZERO) < 0)
			nDelta = nDelta.negate();
		
		final BigDecimal nCommision = TradeUtils.getCommisionValue(nBidPrice, nAskPrice);
		final BigDecimal nMargin = TradeUtils.getMarginValue(nAskPrice, oRateInfo);
		final BigDecimal nMinDelta = nCommision.add(nMargin);
		return (nDelta.compareTo(nMinDelta) < 0);
	}

	public static List<Order> removeFakeOrders(List<Order> oOrders, BigDecimal nMinSum, final RateInfo oRateInfo)
	{
		nMinSum = (null == nMinSum ? BigDecimal.valueOf(TradeUtils.getFakeMinPrice(oRateInfo)) : nMinSum);		
		final List<Order> oResult = new LinkedList<Order>();
		for(final Order oOrder : oOrders)
		{
			if (oOrder.getSum().compareTo(nMinSum) > 0)
				oResult.add(oOrder);
		}
		
		return oResult;
	}

	public static List<Order> removeTooExpenciveOrders(List<Order> oOrders, final RateInfo oRateInfo)
	{
		final List<Order> oResult = removeTopOrders(oOrders);
		if (isDeltaTooSmall(oOrders, oResult, oRateInfo))
			return oOrders;
		
		return oResult;
	}

	public static List<Order> removeFirstTooExpenciveBids(List<Order> oAsks, List<Order> oBids)
	{
		final List<Order> aBidsNext = removeTopOrders(oBids);
		final BigDecimal nBidsDelta = oBids.get(0).getPrice().add(aBidsNext.get(0).getPrice().negate());
		final BigDecimal nBidsAsksDelta = oAsks.get(0).getPrice().add(oBids.get(0).getPrice().negate());
		if (nBidsDelta.compareTo(nBidsAsksDelta) <= 0)
			return oBids;
		
		return removeTopOrders(oBids);
	}

	public static List<Order> removeFirstTooExpenciveAsks(List<Order> oAsks, List<Order> oBids)
	{
		final List<Order> aAsksNext = removeTopOrders(oAsks);
		final BigDecimal nAsksDelta = aAsksNext.get(0).getPrice().add(oAsks.get(0).getPrice().negate());
		final BigDecimal nBidsAsksDelta = oAsks.get(0).getPrice().add(oBids.get(0).getPrice().negate());
		if (nAsksDelta.compareTo(nBidsAsksDelta) <= 0)
			return oBids;
		
		return removeTopOrders(oAsks);
	}

	public static List<Order> removeTopOrders(List<Order> oOrders)
	{
		final BigDecimal nTopPrice = getBestPrice(oOrders);
		final List<Order> oResult = new LinkedList<Order>();
		for(final Order oOrder : oOrders)
		{
			if (oOrder.getPrice().compareTo(nTopPrice) != 0)
				oResult.add(oOrder);
		}
		
		return oResult;
	}

	public static List<Order> removeMyOrders(List<Order> oOrders, List<Order> oMyOrders)
	{
		final List<Order> oResult = new LinkedList<Order>();
		for(final Order oOrder : oOrders)
		{
			boolean bIsMyOrder = false;
			for(final Order oMyOrder : oMyOrders)
			{
				if (null == oMyOrder)
					continue;
				
				if (oOrder.getId().equalsIgnoreCase(oMyOrder.getId()))
					bIsMyOrder = true;
				
				if (StringUtils.isBlank(oOrder.getId()) && oOrder.getPrice().equals(oMyOrder.getPrice()) && oOrder.getVolume().equals(oMyOrder.getVolume()))
					bIsMyOrder = true;
			}
			if (!bIsMyOrder)
				oResult.add(oOrder);
		}
		
		return oResult;
	}
	
	public static List<Order> removeGarbageOrders(List<Order> oOrders, final BigDecimal nLimitPrice, OrderSide oSide)
	{
		final List<Order> oResult = new LinkedList<Order>();
		for(final Order oOrder : oOrders)
		{
			if (oSide.equals(OrderSide.BUY) && oOrder.getPrice().compareTo(nLimitPrice) < 0)
				oResult.add(oOrder);

			if (oSide.equals(OrderSide.SELL) && oOrder.getPrice().compareTo(nLimitPrice) > 0)
				oResult.add(oOrder);
		}
		
		return oResult;
	}
	

	public static BigDecimal getBestPrice(List<Order> oOrders)
	{
		return oOrders.get(0).getPrice();
	}
}

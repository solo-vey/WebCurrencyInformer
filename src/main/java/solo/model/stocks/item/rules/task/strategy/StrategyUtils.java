package solo.model.stocks.item.rules.task.strategy;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.rules.task.trade.TradeUtils;

public class StrategyUtils
{
	public static boolean isDeltaTooSmall(List<Order> oAsks, List<Order> oBids)
	{
		final BigDecimal nAskPrice = getBestPrice(oAsks);
		final BigDecimal nBidPrice = getBestPrice(oBids);
		BigDecimal nDelta = nAskPrice.add(nBidPrice.negate());
		if (nDelta.compareTo(BigDecimal.ZERO) < 0)
			nDelta = nDelta.negate();
		
		final BigDecimal nFullCommision = TradeUtils.getCommisionValue(nBidPrice, nAskPrice);
		return (nDelta.compareTo(nFullCommision) < 0);
	}

	public static List<Order> removeFakeOrders(List<Order> oOrders, final BigDecimal nMinSum)
	{
		final List<Order> oResult = new LinkedList<Order>();
		for(final Order oOrder : oOrders)
		{
			if (oOrder.getSum().compareTo(nMinSum) > 0)
				oResult.add(oOrder);
		}
		
		return oResult;
	}

	public static List<Order> removeTooExpenciveOrders(List<Order> oOrders)
	{
		final List<Order> oResult = removeTopOrders(oOrders);
		if (isDeltaTooSmall(oOrders, oResult))
			return oOrders;
		
		return oResult;
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

	public static BigDecimal getBestPrice(List<Order> oOrders)
	{
		return oOrders.get(0).getPrice();
	}
}
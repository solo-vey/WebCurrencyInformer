package solo.model.stocks.analyse;

import java.math.BigDecimal;
import java.util.List;

import solo.CurrencyInformer;
import solo.model.stocks.BaseObject;
import solo.model.stocks.item.Order;
import solo.utils.MathUtils;
public class OrderAnalysisResult extends BaseObject
{
	protected BigDecimal m_nBestPrice;
	protected BigDecimal m_nAverageAllSumPrice;
	protected BigDecimal m_nAverageHasSumPrice;
	protected BigDecimal m_nBestPriceAllSumPriceDelta;
	protected BigDecimal m_nBestPriceHasSumPriceDelta;
	
	public OrderAnalysisResult(final List<Order> oOrders, final double nAllSum, final double nHasSum)
	{
		if (oOrders.size() == 0)
			return;
		
		m_nBestPrice = oOrders.get(0).getPrice();
		
		double nVolume = 0;
		double nSum = 0;
		for(final Order oOrder : oOrders)
		{
			if (nSum + oOrder.getSum().doubleValue() > nHasSum && (null == m_nAverageHasSumPrice))
			{
				double nOrderNeedSum = (nHasSum - nSum);
				double nOrderNeedVolume = (oOrder.getVolume().doubleValue() * nOrderNeedSum / oOrder.getSum().doubleValue());
				m_nAverageHasSumPrice = MathUtils.getBigDecimal(nHasSum / (nVolume + nOrderNeedVolume), CurrencyInformer.DECIMAL_SCALE);
				
			}
			if (nSum + oOrder.getSum().doubleValue() > nAllSum)
			{
				double nOrderNeedSum = (nAllSum - nSum);
				double nOrderNeedVolume = (oOrder.getVolume().doubleValue() * nOrderNeedSum / oOrder.getSum().doubleValue());
				m_nAverageAllSumPrice = MathUtils.getBigDecimal(nAllSum / (nVolume + nOrderNeedVolume), CurrencyInformer.DECIMAL_SCALE);
				break;
			}

			nVolume += oOrder.getVolume().doubleValue();
			nSum += oOrder.getSum().doubleValue();
		}
		
		m_nBestPriceAllSumPriceDelta = MathUtils.getBigDecimal((m_nBestPrice.doubleValue() - m_nAverageAllSumPrice.doubleValue()) / m_nAverageAllSumPrice.doubleValue(), CurrencyInformer.DECIMAL_SCALE);
		m_nBestPriceHasSumPriceDelta = MathUtils.getBigDecimal((m_nBestPrice.doubleValue() - m_nAverageHasSumPrice.doubleValue()) / m_nAverageHasSumPrice.doubleValue(), CurrencyInformer.DECIMAL_SCALE);
	}

	public BigDecimal getBestPrice()
	{
		return m_nBestPrice;
	}

	public BigDecimal getAverageHasSumPrice()
	{
		return m_nAverageHasSumPrice;
	}

	public BigDecimal getAverageAllSumPrice()
	{
		return m_nAverageAllSumPrice;
	}
}

package solo.model.stocks.analyse;

import java.math.BigDecimal;
import java.util.List;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.Order;
public class OrderAnalysisResult extends BaseObject
{
	protected BigDecimal m_nBestPrice;
	
	public OrderAnalysisResult(final List<Order> oOrders)
	{
		if (oOrders.size() == 0)
			return;
		
		m_nBestPrice = oOrders.get(0).getPrice();
	}

	public BigDecimal getBestPrice()
	{
		return m_nBestPrice;
	}
}

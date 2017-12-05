package solo.model.stocks.item;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.BaseObject;

public class StockUserInfo extends BaseObject
{
	protected Map<Currency, CurrencyAmount> m_oMoney = new HashMap<Currency, CurrencyAmount>();
	protected Map<RateInfo, List<Order>> m_oOrders = new HashMap<RateInfo, List<Order>>();
	
	public Map<Currency, CurrencyAmount> getMoney()
	{
		return m_oMoney;
	}
	
	public Map<RateInfo, List<Order>> getOrders()
	{
		return m_oOrders;
	}
	
	public List<Order> getOrders(final Currency oCurrency)
	{
		return m_oOrders.get(oCurrency);
	}
	
	public void addOrder(final RateInfo oRateInfo, final Order oOrder)
	{
		if (!m_oOrders.containsKey(oRateInfo))
			m_oOrders.put(oRateInfo, new LinkedList<Order>());
		
		m_oOrders.get(oRateInfo).add(oOrder);
	}
}

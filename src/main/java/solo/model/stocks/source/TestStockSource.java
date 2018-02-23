package solo.model.stocks.source;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import solo.CurrencyInformer;
import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.rules.task.trade.ITest;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.ResourceUtils;

public class TestStockSource extends BaseStockSource implements ITest
{
	final IStockSource m_oRealStockSource;
	final TestStockSourceData m_oStockSourceData;
	
	public TestStockSource(final IStockExchange oStockExchange, IStockSource oRealStockSource)
	{
		super(oStockExchange);
		m_oRealStockSource = oRealStockSource;
		m_oStockSourceData = TestStockSourceData.load(oStockExchange);
	}
	
	@Override public RateState getRateState(final RateInfo oRateInfo) throws Exception
	{
		final RateState oRateState = m_oRealStockSource.getCachedRateState(oRateInfo);
		checkOrders(oRateInfo, oRateState);
		return oRateState;
	}
	
	private void checkOrders(final RateInfo oRateInfo, final RateState oRateState)
	{
		final List<Order> oRateOrders = m_oStockSourceData.getRateOrders(oRateInfo);
		if (null == oRateOrders || oRateOrders.size() == 0)
			return;
		
		final Date oLastTradeOrder = m_oStockSourceData.getLastTradeOrder().get(oRateInfo);
		final List<Order> aDoneOrders = new LinkedList<Order>();
		for(final Order oTradeOrder : oRateState.getTrades())
		{
			if (null != oTradeOrder.getCreated() && null != oLastTradeOrder &&
				(oTradeOrder.getCreated().equals(oLastTradeOrder) || oTradeOrder.getCreated().before(oLastTradeOrder)))
				break;
			
			BigDecimal nTradeOrderVolume = oTradeOrder.getVolume();
			for(final Order oOrder : oRateOrders)
			{
				if (nTradeOrderVolume.compareTo(BigDecimal.ZERO) <= 0)
					break;
				
				if (oTradeOrder.getSide().equals(oOrder.getSide()))
					continue;
				
				if (!Order.WAIT.equals(oOrder.getState()))
					continue;
				
				System.err.println(oRateInfo + " TRADE " + oTradeOrder.getInfoShort());
				System.err.println("      " + " Order " + oOrder.getInfoShort());
				if (OrderSide.BUY.equals(oOrder.getSide()) && oOrder.getPrice().compareTo(oTradeOrder.getPrice()) >= 0)
				{
					final BigDecimal nOrderVolumeDelta = oOrder.getVolume().add(nTradeOrderVolume.negate());
					if (nOrderVolumeDelta.compareTo(BigDecimal.ZERO) < 0)
					{
						System.out.println("Done order [" + oOrder.getId() + "] buy [" + oOrder.getVolume() + "] price [" + oOrder.getPrice() + "]");
						oOrder.setState(Order.DONE);
						oOrder.setVolume(BigDecimal.ZERO);
						aDoneOrders.add(oOrder);
						nTradeOrderVolume = nOrderVolumeDelta.negate();
					}
					else
					{
						System.out.println("Order [" + oOrder.getId() + "] buy [" + nTradeOrderVolume + "] price [" + oOrder.getPrice() + "]");
						oOrder.setVolume(nOrderVolumeDelta);
						nTradeOrderVolume = BigDecimal.ZERO;
					}
				}
				
				if (OrderSide.SELL.equals(oOrder.getSide()) && oOrder.getPrice().compareTo(oTradeOrder.getPrice()) <= 0)
				{
					final BigDecimal nOrderVolumeDelta = oOrder.getVolume().add(nTradeOrderVolume.negate());
					if (nOrderVolumeDelta.compareTo(BigDecimal.ZERO) < 0)
					{
						System.out.println("Done order [" + oOrder.getId() + "] sell [" + oOrder.getVolume() + "] price [" + oOrder.getPrice() + "]");
						oOrder.setState(Order.DONE);
						oOrder.setVolume(BigDecimal.ZERO);
						aDoneOrders.add(oOrder);
						nTradeOrderVolume = nOrderVolumeDelta.negate();
					}
					else
					{
						System.out.println("Order [" + oOrder.getId() + "] sell [" + nTradeOrderVolume + "] price [" + oOrder.getPrice() + "]");
						oOrder.setVolume(nOrderVolumeDelta);
						nTradeOrderVolume = BigDecimal.ZERO;
					}
				}
			}
		}
		m_oStockSourceData.getLastTradeOrder().put(oRateInfo, oRateState.getTrades().get(0).getCreated());
		
		for(final Order oOrder : aDoneOrders)
		{
			oRateOrders.remove(oOrder);		
			m_oStockSourceData.addDoneOrder(oOrder);
		}	
		
		m_oStockSourceData.save();
	}

	@Override public StockUserInfo getUserInfo(final RateInfo oRateInfo) throws Exception
	{
		//final StockUserInfo oUserInfo = m_oRealStockSource.getUserInfo(oRateInfo);
		final StockUserInfo oUserInfo = super.getUserInfo(oRateInfo);
		for(final Currency oCurrency : Currency.values())
			oUserInfo.getMoney().put(oCurrency, new CurrencyAmount(new BigDecimal(1000), BigDecimal.ZERO));
		
		oUserInfo.getOrders().putAll(m_oStockSourceData.getRateOrders());
		
		return oUserInfo;
	}
	
	@Override public Order getOrder(final String strOrderId, final RateInfo oRateInfo)
	{
		synchronized (this)
		{			
			for(final Order oOrder : m_oStockSourceData.getRateOrders(oRateInfo))
			{
				if (oOrder.getId().equalsIgnoreCase(strOrderId))
					return oOrder;
			}
			
			for(final Order oOrder : m_oStockSourceData.getDoneOrders())
			{
				if (oOrder.getId().equalsIgnoreCase(strOrderId))
					return oOrder;
			}
			
			for(final Order oOrder : m_oStockSourceData.getRemoveOrders())
			{
				if (oOrder.getId().equalsIgnoreCase(strOrderId))
					return oOrder;
			}	
		}
			
		return new Order(strOrderId, Order.NONE, "Order is absent");
	}
	
	@Override public Order addOrder(final OrderSide oSide, final RateInfo oRateInfo, final BigDecimal nVolume, final BigDecimal nPrice)
	{
		synchronized (this)
		{			
			try
			{
				final Order oOrder = new Order();
				oOrder.setId("test_" + m_oStockSourceData.getLastOrderID() + "_" + (new Date().getTime()));
				oOrder.setSide(oSide);
				oOrder.setVolume(nVolume);
				oOrder.setPrice(nPrice);
				oOrder.setState(Order.WAIT);
				oOrder.setCreated(new Date());
	
				m_oStockSourceData.getRateOrders(oRateInfo).add(oOrder);
				
				System.out.println("Add order complete: " + oOrder.getId() + " " + oOrder.getInfoShort());
				return oOrder;
			}
			catch (Exception e)
			{
				WorkerFactory.onException("TestStockSource.addOrder", e);
				return new Order(Order.EXCEPTION, e.getMessage());
			}
		}
	}
	
	@Override public Order removeOrder(final String strOrderId)
	{
		synchronized (this)
		{			
			super.removeOrder(strOrderId);
			
			for(final List<Order> aOrders : m_oStockSourceData.getRateOrders().values())
			{
				for(final Order oOrder : aOrders)
				{
					if (!oOrder.getId().equalsIgnoreCase(strOrderId))
						continue;
					
					aOrders.remove(oOrder);
					oOrder.setState(Order.CANCEL);
					
					m_oStockSourceData.addRemoveOrder(oOrder);
						
			        System.out.println("Remove order complete. " + strOrderId);
					return oOrder;
				}
			}
			
			return new Order(strOrderId, Order.CANCEL, StringUtils.EMPTY);
		}
	}
}

class TestStockSourceData implements Serializable
{
	private static final long serialVersionUID = -4224697724800230874L;
	
	final Map<RateInfo, List<Order>> m_oRateOrders = new HashMap<RateInfo, List<Order>>();
	final List<Order> m_oRemoveOrders = new LinkedList<Order>();
	final List<Order> m_oDoneOrders = new LinkedList<Order>();
	final Map<RateInfo, Date> m_aLastTradeOrder = new HashMap<RateInfo, Date>();
	Integer m_nLastOrderID = 0;
	
	public TestStockSourceData()
	{
	}
	
	public Integer getLastOrderID()
	{
		if (null == m_nLastOrderID)
			m_nLastOrderID = 0;
		
		return m_nLastOrderID++;
	}

	public Map<RateInfo, List<Order>> getRateOrders()
	{
		return m_oRateOrders;
	}

	public List<Order> getRateOrders(final RateInfo oRateInfo)
	{
		if (null == m_oRateOrders.get(oRateInfo))		
			m_oRateOrders.put(oRateInfo, new LinkedList<Order>());
		
		return m_oRateOrders.get(oRateInfo);
	}

	public List<Order> getRemoveOrders()
	{
		return m_oRemoveOrders;
	}

	public List<Order> getDoneOrders()
	{
		return m_oDoneOrders;
	}
	
	public void addDoneOrder(final Order oOrder)
	{
		m_oDoneOrders.add(oOrder);
		if (m_oDoneOrders.size() > 200)
			m_oDoneOrders.remove(0);
	}
	
	public void addRemoveOrder(final Order oOrder)
	{
		m_oRemoveOrders.add(oOrder);
		if (m_oRemoveOrders.size() > 200)
			m_oRemoveOrders.remove(0);
	}

	public Map<RateInfo, Date> getLastTradeOrder()
	{
		return m_aLastTradeOrder;
	}
	
	public void save()
	{
		try 
		{
	         final FileOutputStream oFileStream = new FileOutputStream(getFileName(WorkerFactory.getStockExchange()));
	         final ObjectOutputStream oStream = new ObjectOutputStream(oFileStream);
	         oStream.writeObject(this);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (IOException e) 
		{
			WorkerFactory.onException("Save test source info exception", e);
		}			
	}

	public static TestStockSourceData load(final IStockExchange oStockExchange)
	{
		try 
		{
	         final FileInputStream oFileStream = new FileInputStream(getFileName(oStockExchange));
	         final ObjectInputStream oStream = new ObjectInputStream(oFileStream);
	         final TestStockSourceData oTestStockSourceData = (TestStockSourceData) oStream.readObject();
	         oStream.close();
	         oFileStream.close();
	         
	         return oTestStockSourceData;
		} 
		catch (final Exception e) 
		{
			WorkerFactory.onException("Load test source info exception", e);
			return new TestStockSourceData();
	    }			
	}

	static String getFileName(final IStockExchange oStockExchange)
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + oStockExchange.getStockName() + "\\TestSource.ser";
	}
}
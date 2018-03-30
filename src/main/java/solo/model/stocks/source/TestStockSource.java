package solo.model.stocks.source;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
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
		if (null == oRateOrders || oRateOrders.size() == 0 || oRateState.getTrades().size() == 0)
			return;
		
		//final DateFormat oDateFormat = new SimpleDateFormat("HH:mm:ss");
		//final String strDate = oDateFormat.format(new Date()) + "\t"; 
		
		final IStockExchange oStockExchange = WorkerFactory.getMainWorker().getStockExchange();
		final String strChance = ResourceUtils.getResource("stock.test.chance", oStockExchange.getStockProperties(), "1");
		final BigDecimal nChance = MathUtils.fromString(strChance);
		
		final Random oRandom = new Random();
		final Date oLastTradeOrder = m_oStockSourceData.getLastTradeOrder().get(oRateInfo);
		final List<Order> aDoneOrders = new LinkedList<Order>();
		for(final Order oTradeOrder : oRateState.getTrades())
		{
			if (null != oTradeOrder.getCreated() && null != oLastTradeOrder &&
				(oTradeOrder.getCreated().equals(oLastTradeOrder) || oTradeOrder.getCreated().before(oLastTradeOrder)))
				break;
			
			if (oRandom.nextDouble() >= nChance.doubleValue())
				continue;
			
			final BigDecimal nTradeOrderVolume = oTradeOrder.getVolume();
			for(final Order oOrder : oRateOrders)
			{				
				if (null != oTradeOrder.getSide() && oTradeOrder.getSide().equals(oOrder.getSide()))
					continue;
				
				if (!Order.WAIT.equals(oOrder.getState()))
					continue;
				
				//System.err.println(strDate + oRateInfo + "\tTRADE " + oTradeOrder.getInfoShort());
				//System.err.println(strDate + "\t" + "Order " + oOrder.getInfoShort() + " - " + oOrder.getId());
				if (OrderSide.BUY.equals(oOrder.getSide()) && oOrder.getPrice().compareTo(oTradeOrder.getPrice()) >= 0)
				{
					final BigDecimal nOrderVolumeDelta = oOrder.getVolume().add(nTradeOrderVolume.negate());
					if (nOrderVolumeDelta.compareTo(BigDecimal.ZERO) < 0)
					{
						//System.err.println(strDate + oRateInfo + " Done order [" + oOrder.getId() + "] buy [" + oOrder.getVolume() + "] price [" + oOrder.getPrice() + "]");
						oOrder.setState(Order.DONE);
						oOrder.setVolume(BigDecimal.ZERO);
						aDoneOrders.add(oOrder);
					}
					else
					{
						//System.err.println(strDate + oRateInfo + " Order [" + oOrder.getId() + "] buy [" + nTradeOrderVolume + "] price [" + oOrder.getPrice() + "]");
						oOrder.setVolume(nOrderVolumeDelta);
					}
				}
				
				if (OrderSide.SELL.equals(oOrder.getSide()) && oOrder.getPrice().compareTo(oTradeOrder.getPrice()) <= 0)
				{
					final BigDecimal nOrderVolumeDelta = oOrder.getVolume().add(nTradeOrderVolume.negate());
					if (nOrderVolumeDelta.compareTo(BigDecimal.ZERO) < 0)
					{
						//System.err.println(strDate + oRateInfo + " Done order [" + oOrder.getId() + "] sell [" + oOrder.getVolume() + "] price [" + oOrder.getPrice() + "]");
						oOrder.setState(Order.DONE);
						oOrder.setVolume(BigDecimal.ZERO);
						aDoneOrders.add(oOrder);
					}
					else
					{
						//System.err.println(strDate + oRateInfo + " Order [" + oOrder.getId() + "] sell [" + nTradeOrderVolume + "] price [" + oOrder.getPrice() + "]");
						oOrder.setVolume(nOrderVolumeDelta);
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
		
		if (aDoneOrders.size() > 0)
			m_oStockSourceData.save();
	}

	@Override public StockUserInfo getUserInfo(final RateInfo oRateInfo) throws Exception
	{
		final StockUserInfo oUserInfo = super.getUserInfo(oRateInfo);
		for(final Currency oCurrency : Currency.values())
			oUserInfo.getMoney().put(oCurrency, new CurrencyAmount(new BigDecimal(10000), BigDecimal.ZERO));
		
		oUserInfo.getOrders().putAll(m_oStockSourceData.getRateOrders());
		
		return oUserInfo;
	}
	
	@Override public Order getOrder(final String strOrderId, final RateInfo oOriginalRateInfo)
	{
		final RateInfo oRateInfo = (oOriginalRateInfo.getIsReverse() ? RateInfo.getReverseRate(oOriginalRateInfo) : oOriginalRateInfo);

		final Order oGetOrder = findOrder(strOrderId, oRateInfo);
		if (null == oGetOrder)
			return new Order(StringUtils.EMPTY, Order.NONE, "Order is absent");
		
		if (!oOriginalRateInfo.getIsReverse())
			return oGetOrder;
			
		return TradeUtils.makeReveseOrder(oGetOrder);			
	}

	protected Order findOrder(final String strOrderId, final RateInfo oRateInfo)
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
		
		return null;
	}
	
	@Override public Order addOrder(final OrderSide oOriginalSide, final RateInfo oOriginalRateInfo, final BigDecimal nOriginalVolume, final BigDecimal nOriginalPrice)
	{
		final RateInfo oRateInfo = (oOriginalRateInfo.getIsReverse() ? RateInfo.getReverseRate(oOriginalRateInfo) : oOriginalRateInfo);
		final OrderSide oSide = (oOriginalRateInfo.getIsReverse() ? (oOriginalSide.equals(OrderSide.SELL) ? OrderSide.BUY : OrderSide.SELL) : oOriginalSide);
		final BigDecimal nVolume = (oOriginalRateInfo.getIsReverse() ? nOriginalVolume.multiply(nOriginalPrice) : nOriginalVolume);
		final BigDecimal nPrice = (oOriginalRateInfo.getIsReverse() ? MathUtils.getBigDecimal(1.0 / nOriginalPrice.doubleValue(), TradeUtils.DEFAULT_PRICE_PRECISION) : nOriginalPrice);
        //if (oOriginalRateInfo.getIsReverse())
        //	System.out.println("Add reverse order: " + oSide + " " + oOriginalRateInfo + " " + nVolume + " " + nPrice);
        
        synchronized (this)
		{			
			try
			{
		        checkOrderParameters(oSide, oRateInfo, nPrice);
		       
		        final Order oOrder = new Order();
				oOrder.setId(oOriginalRateInfo + "_" + m_oStockSourceData.getLastOrderID() + "_" + (new Date().getTime()));
				oOrder.setSide(oSide);
				oOrder.setVolume(nVolume);
				oOrder.setPrice(nPrice);
				oOrder.setState(Order.WAIT);
				oOrder.setCreated(new Date());
	
				m_oStockSourceData.getRateOrders(oRateInfo).add(oOrder);
				m_oStockSourceData.save();
				
				//System.out.println("Add order complete: " + oOrder.getId() + " " + oOrder.getInfoShort());
				
				if (!oOriginalRateInfo.getIsReverse())
					return oOrder;
				
				return TradeUtils.makeReveseOrder(oOrder);
			}
			catch (Exception e)
			{
				WorkerFactory.onException("TestStockSource.addOrder", e);
				return new Order(Order.EXCEPTION, e.getMessage());
			}
		}
	}
	
	@Override public Order removeOrder(final String strOrderId, final RateInfo oOriginalRateInfo)
	{
		synchronized (this)
		{			
			super.removeOrder(strOrderId, oOriginalRateInfo);
			
			for(final List<Order> aOrders : m_oStockSourceData.getRateOrders().values())
			{
				for(final Order oOrder : aOrders)
				{
					if (!oOrder.getId().equalsIgnoreCase(strOrderId))
						continue;
					
					aOrders.remove(oOrder);
					oOrder.setState(Order.CANCEL);
					
					m_oStockSourceData.addRemoveOrder(oOrder);
					m_oStockSourceData.save();
						
			        //System.out.println("Remove order complete. " + strOrderId);
					if (null == oOriginalRateInfo || !oOriginalRateInfo.getIsReverse())
						return oOrder;
					
					return TradeUtils.makeReveseOrder(oOrder);
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
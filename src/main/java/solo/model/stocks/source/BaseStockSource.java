package solo.model.stocks.source;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import solo.CurrencyInformer;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.rules.task.strategy.StrategyUtils;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
import solo.utils.ResourceUtils;

public class BaseStockSource implements IStockSource
{
	final protected List<RateInfo> m_aRates = new LinkedList<RateInfo>();
	final protected List<RateInfo> m_aAllRates = new LinkedList<RateInfo>();
	final protected BigDecimal m_nSumIgnore;
	final protected IStockExchange m_oStockExchange;

	final protected String m_strMoneyUrl;
	final protected String m_strMyOrdersUrl;
	final protected String m_strRemoveOrderUrl;
	final protected String m_strAddOrderUrl;
	final protected String m_strTimeUrl;
	final protected String m_strPublicKey;
	final protected String m_strSecretKey;
	
	final protected Map<RateInfo, RateState> m_oRateStateCache = new HashMap<RateInfo, RateState>();
	
	public BaseStockSource(final IStockExchange oStockExchange)
	{
		m_oStockExchange = oStockExchange;
		m_nSumIgnore = new BigDecimal(ResourceUtils.getIntFromResource("sum.ignore", getStockExchange().getStockProperties(), 1));
		
		m_strMoneyUrl = ResourceUtils.getResource("money.url", getStockExchange().getStockProperties());
		m_strTimeUrl = ResourceUtils.getResource("time.url", getStockExchange().getStockProperties());
		m_strMyOrdersUrl = ResourceUtils.getResource("my_orders.url", getStockExchange().getStockProperties());
		m_strRemoveOrderUrl = ResourceUtils.getResource("remove_order.url", getStockExchange().getStockProperties());
		m_strAddOrderUrl = ResourceUtils.getResource("add_order.url", getStockExchange().getStockProperties());
		
		m_strPublicKey = ResourceUtils.getResource("trade.public.key", getStockExchange().getStockProperties());
		m_strSecretKey = ResourceUtils.getResource("trade.secret.key", getStockExchange().getStockProperties());
		
		load();
	}
	
	public IStockExchange getStockExchange()
	{
		return m_oStockExchange;
	}
	
	public RateState getRateState(final RateInfo oRateInfo) throws Exception
	{
		final RateState oRateState = new RateState(oRateInfo);
		loadRate(oRateInfo, oRateState);
		m_oRateStateCache.put(oRateInfo, oRateState);
		return oRateState; 
	}
		
	protected void loadRate(final RateInfo oRateInfo, final RateState oRateState) throws Exception
	{
	}
	
	public RateState getCachedRateState(final RateInfo oRateInfo) throws Exception
	{
		return m_oRateStateCache.get(oRateInfo);
	}

	public Map<RateInfo, RateStateShort> getAllRateState() throws Exception
	{
		return new HashMap<RateInfo, RateStateShort>();
	}
	
	@Override public StockUserInfo getUserInfo(final RateInfo oRateInfo) throws Exception
	{
		return new StockUserInfo();
	}
	
	@Override public Order addOrder(final OrderSide oSide, final RateInfo oRateInfo, final BigDecimal nVolume, final BigDecimal nPrice)
	{
		return new Order(Order.NONE, "Order is absent");
	}

	public void checkOrderParameters(final OrderSide oSide, final RateInfo oRateInfo, final BigDecimal nPrice) throws Exception
	{
		final StateAnalysisResult oAnalysisResult = m_oStockExchange.getLastAnalysisResult();
		final RateAnalysisResult oRateAnalysisResult = oAnalysisResult.getRateAnalysisResult(oRateInfo);
		
		List<Order> oAsks = oRateAnalysisResult.getAsksOrders(); 
		List<Order> oBids = oRateAnalysisResult.getBidsOrders();
		oAsks = StrategyUtils.removeGarbageOrders(oAsks, oBids.get(0).getPrice(), OrderSide.SELL); 
		oBids = StrategyUtils.removeGarbageOrders(oBids, oAsks.get(0).getPrice(), OrderSide.BUY);
		oAsks = StrategyUtils.removeFakeOrders(oAsks, BigDecimal.ONE, oRateInfo); 
		oBids = StrategyUtils.removeFakeOrders(oBids, BigDecimal.ONE, oRateInfo);
		
		final BigDecimal nMinPrice = MathUtils.getBigDecimal(StrategyUtils.getBestPrice(oBids).doubleValue() * 0.98, TradeUtils.getPricePrecision(oRateInfo));
		final BigDecimal nMaxPrice = MathUtils.getBigDecimal(StrategyUtils.getBestPrice(oAsks).doubleValue() * 1.02, TradeUtils.getPricePrecision(oRateInfo));
		
		if (oSide.equals(OrderSide.SELL) && nPrice.compareTo(nMinPrice) < 0)
		{
			final String strError = "Because price " + MathUtils.toCurrencyString(nPrice) + " is too small. Current [" + MathUtils.toCurrencyString(nMinPrice) + "(-2%)]";
			WorkerFactory.getMainWorker().getLastErrors().addError(strError);
			throw new Exception(strError);
		}

		if (oSide.equals(OrderSide.BUY) && nPrice.compareTo(nMaxPrice) > 0)
		{
			final String strError = "Because price " + MathUtils.toCurrencyString(nPrice) + " is too big. Current [" + MathUtils.toCurrencyString(nMaxPrice) + "(+2%)]";
			WorkerFactory.getMainWorker().getLastErrors().addError(strError);
			throw new Exception(strError);
		}
	}

	@Override public Order getOrder(String strOrderId, final RateInfo oRateInfo)
	{
		return null;
	}

	@Override public Order removeOrder(String strOrderId)
	{
		return null;
	}

	@Override public List<Order> getTrades(RateInfo m_oRateInfo, final int nPage, final int nCount)
	{
		return null;
	}

	public void registerRate(final RateInfo oRateInfo) throws Exception
	{
		if (m_aRates.contains(oRateInfo))
			return;
		
		if (!m_aAllRates.contains(oRateInfo))
			throw new Exception("Unknown rate " + oRateInfo);
		
		m_aRates.add(oRateInfo);
		save();
	}

	public void removeRate(final RateInfo oRateInfo)
	{
		m_aRates.remove(oRateInfo);
		save();
	}

	public List<RateInfo> getRates()
	{
		return m_aRates;
	}

	public List<RateInfo> getAllRates()
	{
		return m_aAllRates;
	}
	
	protected List<Order> convert2Orders(final List<Object> oInputOrders)
	{
		final List<Order> oOrders = new LinkedList<Order>();
		for(final Object oInputOrder : oInputOrders)
		{
			final Order oOrder = convert2Order(oInputOrder);
			oOrders.add(oOrder);
		}
		return oOrders;
	}

	protected Order convert2Order(final Object oInputOrder)
	{
		final Order oOrder = new Order();
		return oOrder;
	}
	
	
	public void save()
	{
		final String strStockEventsFileName = getFileName();

		try 
		{
	         final FileOutputStream oFileStream = new FileOutputStream(strStockEventsFileName);
	         final ObjectOutputStream oStream = new ObjectOutputStream(oFileStream);
	         oStream.writeObject(m_aRates);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (IOException e) 
		{
			WorkerFactory.onException("Save rates exception for stock " + m_oStockExchange.getStockName(), e);
		}			
	}

	@SuppressWarnings("unchecked")
	public void load()
	{
		final String strStockEventsFileName = getFileName(); 
		try 
		{
	         final FileInputStream oFileStream = new FileInputStream(strStockEventsFileName);
	         final ObjectInputStream oStream = new ObjectInputStream(oFileStream);
	         final List<RateInfo> aRates = (List<RateInfo>) oStream.readObject();
	         m_aRates.clear();
	         m_aRates.addAll(aRates);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (final Exception e) 
		{
			WorkerFactory.onException("Load rates exception for stock " + m_oStockExchange.getStockName(), e);
	    }			
	}

	String getFileName()
	{
		final String strStockEventsFileName = ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_oStockExchange.getStockName() + "\\rates.ser";
		return strStockEventsFileName;
	}
}

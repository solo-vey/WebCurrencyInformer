package solo.model.stocks.source;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Collection;
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
import solo.model.stocks.item.OrderTrade;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateParamters;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.rules.task.strategy.StrategyUtils;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
import solo.utils.ResourceUtils;
import solo.utils.TraceUtils;

public class BaseStockSource implements IStockSource
{
	protected final List<RateInfo> m_aRates = new LinkedList<>();
	protected final Map<RateInfo, RateParamters> m_aAllRates = new HashMap<>();
	protected final BigDecimal m_nSumIgnore;
	protected final IStockExchange m_oStockExchange;

	protected final String m_strMoneyUrl;
	protected final String m_strMyOrdersUrl;
	protected final String m_strRemoveOrderUrl;
	protected final String m_strAddOrderUrl;
	protected final String m_strTimeUrl;
	
	protected final String m_strOrdersUrl;
	protected final String m_strTradesUrl;
	protected final String m_strTickerUrl;
	protected final String m_strPairsUrl;
	
	protected final String m_strPublicKey;
	protected final String m_strSecretKey;
	
	protected final Map<RateInfo, RateState> m_oRateStateCache = new HashMap<>();

	public BaseStockSource(final IStockExchange oStockExchange)
	{
		m_oStockExchange = oStockExchange;
		m_nSumIgnore = BigDecimal.valueOf(ResourceUtils.getIntFromResource("sum.ignore", getStockExchange().getStockProperties(), 1));
		
		m_strMoneyUrl = ResourceUtils.getResource("money.url", getStockExchange().getStockProperties());
		m_strTimeUrl = ResourceUtils.getResource("time.url", getStockExchange().getStockProperties());
		m_strMyOrdersUrl = ResourceUtils.getResource("my_orders.url", getStockExchange().getStockProperties());
		m_strRemoveOrderUrl = ResourceUtils.getResource("remove_order.url", getStockExchange().getStockProperties());
		m_strAddOrderUrl = ResourceUtils.getResource("add_order.url", getStockExchange().getStockProperties());

		m_strOrdersUrl = ResourceUtils.getResource("orders.url", getStockExchange().getStockProperties());
		m_strTradesUrl = ResourceUtils.getResource("deals.url", getStockExchange().getStockProperties());
		m_strTickerUrl = ResourceUtils.getResource("ticker.url", getStockExchange().getStockProperties());
		m_strPairsUrl = ResourceUtils.getResource("pairs.url", getStockExchange().getStockProperties());
		
		m_strPublicKey = ResourceUtils.getResource("trade.public.key", getStockExchange().getStockProperties());
		m_strSecretKey = ResourceUtils.getResource("trade.secret.key", getStockExchange().getStockProperties());
		
		load();
		init();
	}
	
	public void init()
	{
		initRates();
	}
	
	protected void initRates()
	{
		/***/
	}
	
	public IStockExchange getStockExchange()
	{
		return m_oStockExchange;
	}
	
	public RateParamters getRateParameters(final RateInfo oRateInfo)
	{
		return m_aAllRates.get(oRateInfo);
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
		/***/
	}
	
	public RateState getCachedRateState(final RateInfo oRateInfo) throws Exception
	{
		return m_oRateStateCache.get(oRateInfo);
	}
	
	public void setCachedRateState(final RateInfo oRateInfo, final RateState oRateState) throws Exception
	{
		m_oRateStateCache.put(oRateInfo, oRateState);
	}

	public Map<RateInfo, RateStateShort> getAllRateState() throws Exception
	{
		return new HashMap<>();
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
		oAsks = StrategyUtils.removeFakeOrders(oAsks, null, oRateInfo); 
		oBids = StrategyUtils.removeFakeOrders(oBids, null, oRateInfo);
		
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

	@Override public Order removeOrder(String strOrderId, final RateInfo oRateInfo)
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
		
		if (!m_aAllRates.containsKey(oRateInfo))
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

	public Collection<RateInfo> getAllRates()
	{
		return m_aAllRates.keySet();
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
		return new Order();
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
			TraceUtils.writeError("Save rates exception for stock " + m_oStockExchange.getStockName(), e);
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
			TraceUtils.writeError("Load rates exception for stock " + m_oStockExchange.getStockName(), e);
	    }			
	}

	String getFileName()
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + m_oStockExchange.getStockName() + "\\rates.ser";
	}

	protected OrderTrade convert2Trade(final Object oInputTrade, final RateInfo bIsReverse)
	{
		return new OrderTrade();
	}

	@Override public List<OrderTrade> getTrades(final String strOrderID, final RateInfo oRateInfo)
	{
		return null;
	}
}

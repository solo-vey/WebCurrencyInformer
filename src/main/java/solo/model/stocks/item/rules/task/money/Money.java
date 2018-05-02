package solo.model.stocks.item.rules.task.money;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.CurrencyInformer;
import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.source.IStockSource;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;
import solo.utils.ResourceUtils;

public class Money implements Serializable
{
	private static final long serialVersionUID = -2095455737411332587L;
	
	public static String SYNCHRONIZE_INFO = "info";
	public static String SYNCHRONIZE_FULL = "synchronize";
	
	protected Map<Currency, BigDecimal> m_oMoney = new HashMap<Currency, BigDecimal>();
	protected List<TradeMoney> m_oReserveMoney = new LinkedList<TradeMoney>();
	
	protected List<TradeMoney> getReserveMoney()
	{
		if (null == m_oReserveMoney)
			m_oReserveMoney = new LinkedList<TradeMoney>();
		
		return m_oReserveMoney;
	}
	
	public BigDecimal getFreeMoney(final Currency oCurrency)
	{
		final BigDecimal nHaveSum = m_oMoney.get(oCurrency);
		return (null == nHaveSum ? BigDecimal.ZERO : nHaveSum);
	}
	
	public TradeMoney reserveMoney(final RateInfo oRateInfo, final BigDecimal nSum)
	{
		synchronized (this)
		{
			final Currency oSumCurrency = oRateInfo.getCurrencyTo();
			final BigDecimal nHaveSum = getFreeMoney(oSumCurrency);
			
			if (null == nHaveSum || nHaveSum.compareTo(nSum) < 0)
				return null;
						
			final TradeMoney oTradeMoney = new TradeMoney(BigDecimal.ZERO, nSum, oRateInfo);
			m_oMoney.put(oSumCurrency, nHaveSum.add(nSum.negate()));
			getReserveMoney().add(oTradeMoney);
			
			save();
			return oTradeMoney;			
		}
	}
	
	public void freeMoney(final ITradeControler oTradeControler)
	{
		synchronized (this)
		{
			final TradeMoney oTradeMoney = findTradeMoney(oTradeControler);
			final RateInfo oRateInfo = oTradeControler.getTradesInfo().getRateInfo();
			freeMoney(oRateInfo, oTradeMoney);
		}		
	}
	
	public void freeMoney(final RateInfo oRateInfo, final TradeMoney oTradeMoney)
	{
		synchronized (this)
		{
			if (null == oTradeMoney)
			{
				System.out.println("Can't free money for [" + oRateInfo + "] [" + oTradeMoney + "]");
				return;
			}
			
			final Currency oSumCurency = oRateInfo.getCurrencyTo();
			final BigDecimal nHaveSum = m_oMoney.get(oSumCurency);
			m_oMoney.put(oSumCurency, nHaveSum.add(oTradeMoney.getSum()));
			
			final Currency oVolumeCurency = oRateInfo.getCurrencyFrom();
			final BigDecimal nHaveVolume = m_oMoney.get(oVolumeCurency);
			m_oMoney.put(oVolumeCurency, nHaveVolume.add(oTradeMoney.getVolume()));
			
			getReserveMoney().remove(oTradeMoney);
			
			save();
		}		
	}
	
	public TradeMoney findTradeMoney(final ITradeControler oTradeControler)
	{
		for(final TradeMoney oTradeMoney : getReserveMoney())
		{
			if (oTradeMoney.getTradeID().equals(oTradeControler.getTradesInfo().getRuleID()))
				return oTradeMoney;
		}
		
		System.out.println("Can't find TradeControler  ID [" + oTradeControler.getTradesInfo().getRuleID() + "] [" + oTradeControler.getTradesInfo().getRateInfo() + "]");
		return null;
	}
	
	public String synchonize(final String strType)
	{
		synchronized (this)
		{
			String strResult = StringUtils.EMPTY;
			
			try
			{
				final IStockSource oStockSource = WorkerFactory.getStockSource();
				final StockUserInfo oUserInfo = oStockSource.getUserInfo(null);
				
				strResult = "Synchonize money.\r\n";
				if (SYNCHRONIZE_INFO.equalsIgnoreCase(strType))
					strResult += "Currency / Real / Stock / Reserved\r\n";
				
				final List<TradeMoney> oLostMoney = new LinkedList<TradeMoney>();
				for(final TradeMoney oTradeMoney : getReserveMoney())
				{
					final int nRuleID = oTradeMoney.getTradeID();
					if (!WorkerFactory.getStockExchange().getRules().getRules().containsKey(nRuleID))
						oLostMoney.add(oTradeMoney);
				}
				
				for(final TradeMoney oTradeMoney : oLostMoney)
				{
					getReserveMoney().remove(oTradeMoney);
					strResult += "Remove lost money - " + oTradeMoney.getRateInfo() + " / ";
					strResult += " / " + MathUtils.toCurrencyStringEx3(oTradeMoney.getSum()) + " / " + MathUtils.toCurrencyStringEx3(oTradeMoney.getVolume()) + " / " ;
					strResult += "\r\n";
				}

				for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oUserInfo.getMoney().entrySet())
				{
					final Currency oCurrency = oCurrencyInfo.getKey();
					final BigDecimal nFreeMoney = getFreeMoney(oCurrency);
					BigDecimal nLockedMoney = BigDecimal.ZERO;
					for(final TradeMoney oTradeMoney : getReserveMoney())
					{
						final RateInfo oRateInfo = oTradeMoney.getRateInfo();
						if (oRateInfo.getCurrencyFrom().equals(oCurrency))
							nLockedMoney = nLockedMoney.add(oTradeMoney.getVolume());
						if (oRateInfo.getCurrencyTo().equals(oCurrency))
							nLockedMoney = nLockedMoney.add(oTradeMoney.getSum());
					}

					final BigDecimal nRealMoney = oCurrencyInfo.getValue().getBalance().add(oCurrencyInfo.getValue().getLocked());
					final BigDecimal nAllMoney = nFreeMoney.add(nLockedMoney);
					
					final boolean bIsNotEqual = (nAllMoney.compareTo(nRealMoney) != 0 );
					if (bIsNotEqual && SYNCHRONIZE_FULL.equalsIgnoreCase(strType))
					{
						m_oMoney.put(oCurrency, nRealMoney.add(nLockedMoney.negate()));						
						strResult += oCurrency + " : real [" + nRealMoney + "] != stock [" + nAllMoney+ "]\r\n";
					}
					
					if (SYNCHRONIZE_INFO.equalsIgnoreCase(strType))
					{
						strResult += (bIsNotEqual ? "<code>" + oCurrency.toString().toUpperCase() : oCurrency.toString());
						strResult += " / " + MathUtils.toCurrencyStringEx3(nRealMoney) + " / " + MathUtils.toCurrencyStringEx3(nAllMoney) + " / " ;
						for(final TradeMoney oTradeMoney : getReserveMoney())
						{
							final RateInfo oRateInfo = oTradeMoney.getRateInfo();
							if (oRateInfo.getCurrencyFrom().equals(oCurrency))
								strResult += oRateInfo.getCurrencyTo() + ":" + MathUtils.toCurrencyStringEx3(oTradeMoney.getVolume()) + ", ";
							if (oRateInfo.getCurrencyTo().equals(oCurrency))
								strResult += oRateInfo.getCurrencyFrom() + ":" + MathUtils.toCurrencyStringEx3(oTradeMoney.getSum()) + ", ";
						}
						strResult += (bIsNotEqual ? "</code>" : StringUtils.EMPTY) + "\r\n";
					}
				}
				strResult += "Complete";
				
				System.out.println(strResult);
				save();
			}
			catch(final Exception e)
			{
				strResult = "Error synchronize money";
				WorkerFactory.onException(strResult, e);
			}
			
			return strResult;
		}
	}
	
	public void addBuy(final ITradeControler oTradeControler, final BigDecimal nSpendSum, final BigDecimal nBuyVolume)
	{
		if (null == oTradeControler || ManagerUtils.isTestObject(oTradeControler))
			return;
		
		final TradeMoney oTradeMoney = findTradeMoney(oTradeControler);
		if (null == oTradeMoney)
			return;

		synchronized (oTradeMoney)
		{
			oTradeMoney.addSum(nSpendSum.negate());
			oTradeMoney.addVolume(nBuyVolume);
		}		
		save();
	}
	
	public void addSell(final ITradeControler oTradeControler, final BigDecimal nReceiveSum, final BigDecimal nSoldVolume)
	{
		if (null == oTradeControler || ManagerUtils.isTestObject(oTradeControler))
			return;
		
		final TradeMoney oTradeMoney = findTradeMoney(oTradeControler);
		if (null == oTradeMoney)
			return;

		synchronized (oTradeMoney)
		{
			oTradeMoney.addSum(nReceiveSum);
			oTradeMoney.addVolume(nSoldVolume.negate());
		}		
		save();
	}
	
	public void save()
	{
		synchronized (this)
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
				WorkerFactory.onException("Save money exception", e);
			}
		}
	}

	public static Money load(final IStockExchange oStockExchange)
	{
		try 
		{
	         final FileInputStream oFileStream = new FileInputStream(getFileName(oStockExchange));
	         final ObjectInputStream oStream = new ObjectInputStream(oFileStream);
	         final Money oMoney = (Money) oStream.readObject();
	         oStream.close();
	         oFileStream.close();
	         
	         return oMoney;
		} 
		catch (final Exception e) 
		{
			WorkerFactory.onException("Load money exception", e);
			return new Money();
	    }			
	}
	
	protected static String getFileName(final IStockExchange oStockExchange)
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + oStockExchange.getStockName() + "\\money.ser";
	}
}

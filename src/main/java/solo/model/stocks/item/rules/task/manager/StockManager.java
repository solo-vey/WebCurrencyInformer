package solo.model.stocks.item.rules.task.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import solo.CurrencyInformer;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.worker.WorkerFactory;
import ua.lz.ep.utils.ResourceUtils;

public class StockManager implements IStockManager
{
	final protected StockManagesInfo m_oStockManagesInfo;
	
	public StockManager(final IStockExchange oStockExchange)
	{
		m_oStockManagesInfo = load(oStockExchange);
	}
	
	public void manage(final StateAnalysisResult oStateAnalysisResult) 
	{
		
	}
	
	@Override public StockManagesInfo getInfo()
	{
		return m_oStockManagesInfo;
	}
	
	@Override public void tradeStart(final TaskTrade oTaskTrade) 
	{
		m_oStockManagesInfo.tradeStart(oTaskTrade);
		save();
	}
	
	@Override public void tradeDone(final TaskTrade oTaskTrade) 
	{
		m_oStockManagesInfo.tradeDone(oTaskTrade);
		save();
	}
	
	@Override public void buyDone(final TaskTrade oTaskTrade) 
	{
		m_oStockManagesInfo.buyDone(oTaskTrade);
		save();
	}
	
	@Override public void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume) 
	{
		m_oStockManagesInfo.addBuy(nSpendSum, nBuyVolume);
		save();
	}
	
	@Override public void addSell(final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) 
	{
		m_oStockManagesInfo.addSell(nReceiveSum, nSoldVolume);
		save();
	} 
	
	public void save()
	{
		try 
		{
	         final FileOutputStream oFileStream = new FileOutputStream(getFileName(WorkerFactory.getStockExchange()));
	         final ObjectOutputStream oStream = new ObjectOutputStream(oFileStream);
	         oStream.writeObject(m_oStockManagesInfo);
	         oStream.close();
	         oFileStream.close();
		} 
		catch (IOException e) 
		{
			WorkerFactory.onException("Save manager info exception", e);
		}			
	}

	public StockManagesInfo load(final IStockExchange oStockExchange)
	{
		try 
		{
	         final FileInputStream oFileStream = new FileInputStream(getFileName(oStockExchange));
	         final ObjectInputStream oStream = new ObjectInputStream(oFileStream);
	         final StockManagesInfo oStockManagesInfo = (StockManagesInfo) oStream.readObject();
	         oStream.close();
	         oFileStream.close();
	         
	         return oStockManagesInfo;
		} 
		catch (final Exception e) 
		{
			WorkerFactory.onException("Load manager info exception", e);
			return new StockManagesInfo();
	    }			
	}

	String getFileName(final IStockExchange oStockExchange)
	{
		return ResourceUtils.getResource("events.root", CurrencyInformer.PROPERTIES_FILE_NAME) + "\\" + oStockExchange.getStockName() + "\\manager.ser";
	}
}

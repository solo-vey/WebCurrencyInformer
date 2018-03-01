package solo.model.stocks.item.rules.task.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.CurrencyInformer;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.command.system.AddRateCommand;
import solo.model.stocks.item.rules.task.trade.ITest;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;
import solo.utils.ResourceUtils;

public class StockManager implements IStockManager
{
	final protected StockManagesInfo m_oStockManagesInfo;
	final protected boolean m_bIsTrackTrades;
	
	public StockManager(final IStockExchange oStockExchange)
	{
		m_oStockManagesInfo = load(oStockExchange);
		m_bIsTrackTrades = true;
	}
	
	public void manage(final StateAnalysisResult oStateAnalysisResult) 
	{
		startTestControler();
		lookForProspectiveRate();
	}	
	
	protected void startTestControler()
	{
		for(final RateInfo oRateInfo : WorkerFactory.getStockSource().getRates())
			ManagerUtils.createTestControler(oRateInfo);
	}
	
	private void lookForProspectiveRate()
	{
		try
		{
			final Map<RateInfo, RateStateShort> oAllRateState = WorkerFactory.getStockSource().getAllRateState();
			final List<RateInfo> oProspectiveRates = ManagerUtils.getProspectiveRates(oAllRateState, BigDecimal.ZERO);
//			for(final RateInfo oRateInfo : oProspectiveRates)
//				WorkerFactory.getMainWorker().addCommand(new AddRateCommand(oRateInfo.toString()));
		}
		catch(final Exception e)
		{
			WorkerFactory.onException("Can't create prospective rates list.", e);
		}
	}

	void trackTrades(final TaskTrade oTaskTrade)
	{
		if (!m_bIsTrackTrades)
			return;
		
		final BigDecimal nTradeDelta = oTaskTrade.getTradeInfo().getDelta();
		final BigDecimal nMargin = TradeUtils.getMarginValue(oTaskTrade.getTradeInfo().getReceivedSum(), oTaskTrade.getTradeInfo().getRateInfo());
		if (nTradeDelta.compareTo(nMargin) < 0)
			stopControler(oTaskTrade.getTradeInfo().getRateInfo(), oTaskTrade);
		else
			startAllControlers(oTaskTrade.getTradeInfo().getRateInfo(), oTaskTrade);
	}
	
	private void stopControler(final RateInfo oRateInfo, final TaskTrade oTaskTrade)
	{
		if (oTaskTrade.getTradeControler() instanceof ITest)
			return;
		
		final List<Entry<Integer, IRule>> oRules = WorkerFactory.getStockExchange().getRules().getRules(oRateInfo);
		final List<ITradeControler> aWorkingControler = new LinkedList<ITradeControler>();
		for(final Entry<Integer, IRule> oRuleInfo : oRules)
		{
			final IRule oRule = oRuleInfo.getValue();
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
			if (null == oControler)
				continue;
			
			if (oControler.getParameter(TradeControler.TRADE_COUNT_PARAMETER).equalsIgnoreCase("0"))
				continue;
			
			final DateFormat oDateFormat = new SimpleDateFormat("dd.MM HH:mm:ss");
			oControler.getTradesInfo().getHistory().addToLog(oDateFormat.format(new Date()) + " Stop controler [" + oRateInfo + "]");

			aWorkingControler.add(oControler);
		}
		
		if (aWorkingControler.size() > 1)
		{
			oTaskTrade.getTradeControler().setParameter(TradeControler.TRADE_COUNT_PARAMETER, "0");
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.TRACE, "MANAGER\r\nStop controler [" + oRateInfo + "]");
		}
	}
		
	private void startAllControlers(final RateInfo oRateInfo, final TaskTrade oTaskTrade)
	{
		final List<Entry<Integer, IRule>> oRules = WorkerFactory.getStockExchange().getRules().getRules(oRateInfo);
		String strMessage = StringUtils.EMPTY  + "";
		for(final Entry<Integer, IRule> oRuleInfo : oRules)
		{
			final IRule oRule = oRuleInfo.getValue();
			final ITradeControler oControler = TradeUtils.getRuleAsTradeControler(oRule);
			if (null == oControler)
				continue;
			
			final String strMaxTrades = oControler.getParameter(TradeControler.TRADE_COUNT_PARAMETER);
			if (!strMaxTrades.equalsIgnoreCase("0"))
				continue;
			
			final DateFormat oDateFormat = new SimpleDateFormat("dd.MM HH:mm:ss");
			oControler.getTradesInfo().getHistory().addToLog(oDateFormat.format(new Date()) + " Manager.startAllControlers.");
			strMessage += "Start controler [" + oRateInfo + "]\r\n";	
			oControler.setParameter(TradeControler.TRADE_COUNT_PARAMETER, oControler.getParameter(TradeControler.MAX_TARDES));
		}
		
		if (StringUtils.isNotBlank(strMessage))
			WorkerFactory.getMainWorker().sendMessage(MessageLevel.TRACE, "MANAGER\r\n" + strMessage);
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
		trackTrades(oTaskTrade);
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
	}
	
	@Override public void addSell(final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) 
	{
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

package solo.model.stocks.item.command.trade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.rule.CheckRateRulesCommand;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class GetStockInfoCommand extends BaseCommand
{
	final static public String NAME = "info";
	final static public String RATE_PARAMETER = "#rate#";
	
	public GetStockInfoCommand(final String strСommandLine)
	{
		super(strСommandLine, RATE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		String strMessage = StringUtils.EMPTY;
		WorkerFactory.getMainWorker().sendSystemMessage("Calculate stock info ...");
		
		try
		{		
			final StockUserInfo oUserInfo = WorkerFactory.getStockExchange().getStockSource().getUserInfo(null);
			
			final Map<RateInfo, RateAnalysisResult> oRateHash = new HashMap<RateInfo, RateAnalysisResult>();
			for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oUserInfo.getMoney().entrySet())
			{
				if (oCurrencyInfo.getValue().getBalance().compareTo(new BigDecimal(0.000001)) < 0 && 
					oCurrencyInfo.getValue().getLocked().compareTo(new BigDecimal(0.000001)) < 0)
						continue;
				
				strMessage += oCurrencyInfo.getKey() + "/" + MathUtils.toCurrencyStringEx3(oCurrencyInfo.getValue().getBalance()) + 
								(oCurrencyInfo.getValue().getLocked().compareTo(BigDecimal.ZERO) != 0 ? "/" + MathUtils.toCurrencyStringEx3(oCurrencyInfo.getValue().getLocked()) : StringUtils.EMPTY)
								+ "\r\n";
			}
			strMessage += "\r\n";
	
			for(final Entry<RateInfo, List<Order>> oOrdersInfo : oUserInfo.getOrders().entrySet())
			{
				for(final Order oOrder : oOrdersInfo.getValue())
					strMessage += oOrdersInfo.getKey() + "/" + oOrder.getInfo() + "\r\n";
			}
			strMessage += "\r\n";
			WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
			
			BigDecimal oTotalBtcSum = BigDecimal.ZERO;
			final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
			for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oUserInfo.getMoney().entrySet())
			{
				if (oCurrencyInfo.getKey().equals(Currency.BTC))
				{
					oTotalBtcSum = oTotalBtcSum.add(oCurrencyInfo.getValue().getBalance());
					continue;
				}
				
				final RateAnalysisResult oBtcToCurrencyRate = getRate(oStockExchange, oCurrencyInfo.getKey(), oRateHash);
				if (null == oBtcToCurrencyRate)
					continue;
						
				final BigDecimal oBtcBidPrice = oBtcToCurrencyRate.getBidsAnalysisResult().getBestPrice();
				final BigDecimal oVolume = oCurrencyInfo.getValue().getBalance();
				final BigDecimal oSum = oVolume.multiply(oBtcBidPrice);
				oTotalBtcSum = oTotalBtcSum.add(oSum);
			}
	
			for(final Entry<RateInfo, List<Order>> oOrdersInfo : oUserInfo.getOrders().entrySet())
			{
				final RateAnalysisResult oBtcToCurrencyRate = getRate(oStockExchange, oOrdersInfo.getKey().getCurrencyTo(), oRateHash);
				if (null == oBtcToCurrencyRate)
					continue;
					
				for(final Order oOrder : oOrdersInfo.getValue())
				{
					if (oOrdersInfo.getKey().getCurrencyFrom().equals(Currency.BTC))
					{
						oTotalBtcSum = oTotalBtcSum.add(oOrder.getVolume());
						continue;
					}
							
					final BigDecimal oBtcBidPrice = oBtcToCurrencyRate.getBidsAnalysisResult().getBestPrice();
					final BigDecimal oSum = oOrder.getSum().multiply(oBtcBidPrice);
					oTotalBtcSum = oTotalBtcSum.add(oSum);
				}
			}
			
			strMessage += "Total BTC = " + MathUtils.toCurrencyStringEx3(oTotalBtcSum) + "\r\n";
			
			final RateAnalysisResult oBtcToUahRate = getRate(oStockExchange, Currency.UAH, oRateHash);
			if (null != oBtcToUahRate)
			{
				final BigDecimal oBtcBidPrice = oBtcToUahRate.getBidsAnalysisResult().getBestPrice();
				final BigDecimal oTotalUahSum = MathUtils.getBigDecimal(oTotalBtcSum.doubleValue() / oBtcBidPrice.doubleValue(), TradeUtils.DEFAULT_PRICE_PRECISION);
				strMessage += "Total UAH = " + MathUtils.toCurrencyStringEx3(oTotalUahSum) + "\r\n";
			}
		}
		catch(final Exception e)
		{
			strMessage = e.getMessage();
		}

		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}

	protected RateAnalysisResult getRate(final IStockExchange oStockExchange, final Currency oCurrency, final Map<RateInfo, RateAnalysisResult> oRateHash) throws Exception
	{
		final RateInfo oRateInfo = new RateInfo(oCurrency, Currency.BTC);
		final RateAnalysisResult oBtcToCurrencyRate = oStockExchange.getLastAnalysisResult().getRateAnalysisResult(oRateInfo);
		if (null != oBtcToCurrencyRate)
			return oBtcToCurrencyRate;
		
		return loadRate(oStockExchange, oRateInfo, oRateHash);
	}

	protected RateAnalysisResult loadRate(final IStockExchange oStockExchange, final RateInfo oRateInfo, final Map<RateInfo, RateAnalysisResult> oRateHash) throws Exception
	{
		if (oRateHash.containsKey(oRateInfo))
			return oRateHash.get(oRateInfo);
		
		try
		{
			final RateState oRateState = oStockExchange.getStockSource().getRateState(oRateInfo);
			final RateAnalysisResult oRateAnalysisResult = new RateAnalysisResult(oRateState, oRateInfo, oStockExchange);
			oRateHash.put(oRateInfo, oRateAnalysisResult);
			return oRateAnalysisResult;
		}
		catch(final Exception e)
		{
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo);
			final RateState oReverseRateState = oStockExchange.getStockSource().getRateState(oReverseRateInfo);
			final RateState oRateState = CheckRateRulesCommand.makeReverseRateState(oReverseRateState);
			final RateAnalysisResult oRateAnalysisResult = new RateAnalysisResult(oRateState, oRateInfo, oStockExchange);
			oRateHash.put(oRateInfo, oRateAnalysisResult);
			return oRateAnalysisResult;
		}

	}
}

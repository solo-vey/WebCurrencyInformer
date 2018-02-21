package solo.model.stocks.item.command.trade;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.telegram.TelegramTransport;
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
		final Map<RateInfo, RateStateShort> oAllRateState = WorkerFactory.getStockExchange().getStockSource().getAllRateState();
		
		try
		{		
			final Rules oRules = WorkerFactory.getStockExchange().getRules();
			final StockUserInfo oUserInfo = WorkerFactory.getStockExchange().getStockSource().getUserInfo(null);
			
		   	final List<List<String>> aButtons = new LinkedList<List<String>>();
			for(final Entry<RateInfo, List<Order>> oOrdersInfo : oUserInfo.getOrders().entrySet())
			{
				for(final Order oOrder : oOrdersInfo.getValue())
				{
					final String strOrderInfo = oOrdersInfo.getKey() + "/" + oOrder.getSide() + "/" + MathUtils.toCurrencyStringEx3(oOrder.getPrice()) + 
													"/" + MathUtils.toCurrencyStringEx3(oOrder.getSum());
					aButtons.add(Arrays.asList(strOrderInfo + " [X]=" + CommandFactory.makeCommandLine(RemoveOrderCommand.class, RemoveOrderCommand.ID_PARAMETER, oOrder.getId())));
				}
			}
			
			final Map<RateInfo, RateAnalysisResult> oRateHash = new HashMap<RateInfo, RateAnalysisResult>();	
			for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oUserInfo.getMoney().entrySet())
			{
				if (oCurrencyInfo.getValue().getBalance().compareTo(new BigDecimal(0.000001)) < 0 && 
					oCurrencyInfo.getValue().getLocked().compareTo(new BigDecimal(0.000001)) < 0)
						continue;
				
				BigDecimal nFreeVolum = oCurrencyInfo.getValue().getBalance();
				for(final IRule oRule : oRules.getRules().values())
				{
					final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRule);
					if (null == oTradeTask)
						continue;
					
					final Order oOrder = oTradeTask.getTradeInfo().getOrder();
					if (null == oOrder)
						continue;
					
					if (OrderSide.BUY.equals(oOrder.getSide()) && oTradeTask.getRateInfo().getCurrencyFrom().equals(oCurrencyInfo.getKey()))
						nFreeVolum = nFreeVolum.add(oTradeTask.getTradeInfo().getBoughtVolume().negate());
					
					if (OrderSide.SELL.equals(oOrder.getSide()) && oTradeTask.getRateInfo().getCurrencyTo().equals(oCurrencyInfo.getKey()))
						nFreeVolum = nFreeVolum.add(oTradeTask.getTradeInfo().getReceivedSum().negate());
				}
				strMessage += oCurrencyInfo.getKey() + "/" + MathUtils.toCurrencyStringEx3(oCurrencyInfo.getValue().getBalance()) + 
								"/" + MathUtils.toCurrencyStringEx3(nFreeVolum) + "\r\n";
				
				if (nFreeVolum.compareTo(BigDecimal.ZERO) > 0)
				{
					for(final Entry<RateInfo, RateStateShort> oShortRateInfo : oAllRateState.entrySet())
					{
						if (!oShortRateInfo.getKey().getCurrencyFrom().equals(oCurrencyInfo.getKey()))
							continue;
						
						if (TradeUtils.getMinTradeVolume(oShortRateInfo.getKey()).compareTo(nFreeVolum) > 0)
							continue;
						
						if (!oUserInfo.getMoney().containsKey(oShortRateInfo.getKey().getCurrencyTo()))
							continue;
						
						final BigDecimal nPrice = oShortRateInfo.getValue().getAskPrice();
						final BigDecimal nSum = nPrice.multiply(nFreeVolum);
						aButtons.add(Arrays.asList(oShortRateInfo.getKey() + "/" + MathUtils.toCurrencyStringEx3(nFreeVolum) + 
								"/" + MathUtils.toCurrencyStringEx3(nPrice) + "/" + MathUtils.toCurrencyStringEx3(nSum) + " [+]=" + 
							CommandFactory.makeCommandLine(AddOrderCommand.class, AddOrderCommand.SIDE_PARAMETER, OrderSide.SELL,
									AddOrderCommand.RATE_PARAMETER, oShortRateInfo.getKey(), 
									AddOrderCommand.PRICE_PARAMETER, MathUtils.toCurrencyStringEx3(nPrice).replace(",", StringUtils.EMPTY), 
									AddOrderCommand.VOLUME_PARAMETER,  MathUtils.toCurrencyStringEx3(nFreeVolum))));
					}
				}
			}
			strMessage += "\r\n";
			
			BigDecimal oTotalBtcSum = BigDecimal.ZERO;
			final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
			final Map<Currency, BigDecimal> aLocked = new HashMap<Currency, BigDecimal>();
			for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oUserInfo.getMoney().entrySet())
			{
				aLocked.put(oCurrencyInfo.getKey(), oCurrencyInfo.getValue().getLocked());
				if (oCurrencyInfo.getKey().equals(Currency.BTC))
				{
					oTotalBtcSum = oTotalBtcSum.add(oCurrencyInfo.getValue().getBalance());
					continue;
				}
				
				final BigDecimal oBtcBidPrice = getRateBestBidPrice(oStockExchange, oCurrencyInfo.getKey(), oRateHash, oAllRateState);
				if (null == oBtcBidPrice)
					continue;
						
				final BigDecimal oVolume = oCurrencyInfo.getValue().getBalance();
				final BigDecimal oSum = oVolume.multiply(oBtcBidPrice);
				oTotalBtcSum = oTotalBtcSum.add(oSum);
			}

			for(final Entry<RateInfo, List<Order>> oOrdersInfo : oUserInfo.getOrders().entrySet())
			{
				final BigDecimal oBtcBidPrice = getRateBestBidPrice(oStockExchange, oOrdersInfo.getKey().getCurrencyTo(), oRateHash, oAllRateState);
				if (null == oBtcBidPrice)
					continue;
					
				BigDecimal nLockedVolume = aLocked.get(oOrdersInfo.getKey().getCurrencyFrom());
				BigDecimal nLockedSum = aLocked.get(oOrdersInfo.getKey().getCurrencyTo());
				for(final Order oOrder : oOrdersInfo.getValue())
				{
					if (OrderSide.SELL.equals(oOrder.getSide()))
						nLockedVolume = nLockedVolume.add(oOrder.getVolume().negate());
					
					if (OrderSide.BUY.equals(oOrder.getSide()))
						nLockedSum = nLockedSum.add(oOrder.getSum().negate());

					if (oOrdersInfo.getKey().getCurrencyFrom().equals(Currency.BTC))
					{
						oTotalBtcSum = oTotalBtcSum.add(oOrder.getVolume());
						continue;
					}
							
					final BigDecimal oSum = oOrder.getSum().multiply(oBtcBidPrice);
					oTotalBtcSum = oTotalBtcSum.add(oSum);
				}
				
				aLocked.put(oOrdersInfo.getKey().getCurrencyFrom(), nLockedVolume);
				aLocked.put(oOrdersInfo.getKey().getCurrencyTo(), nLockedSum);
			}
			
			for(final Entry<Currency, BigDecimal> oLockedInfo : aLocked.entrySet())
			{
				if (oLockedInfo.getValue().compareTo(BigDecimal.ZERO) == 0)
					continue;
				
				if (oLockedInfo.getKey().equals(Currency.BTC))
				{
					oTotalBtcSum = oTotalBtcSum.add(oLockedInfo.getValue());
					continue;
				}
				
				final BigDecimal oBtcBidPrice = getRateBestBidPrice(oStockExchange, oLockedInfo.getKey(), oRateHash, oAllRateState);
				if (null == oBtcBidPrice)
					continue;
						
				final BigDecimal oVolume = oLockedInfo.getValue();
				final BigDecimal oSum = oVolume.multiply(oBtcBidPrice);
				oTotalBtcSum = oTotalBtcSum.add(oSum);
			}
			
			strMessage += "Total BTC = " + MathUtils.toCurrencyStringEx3(oTotalBtcSum) + "\r\n";
			
			final BigDecimal oBtcBidPrice = getRateBestBidPrice(oStockExchange, Currency.UAH, oRateHash, oAllRateState);
			if (null != oBtcBidPrice)
			{
				final BigDecimal oTotalUahSum = MathUtils.getBigDecimal(oTotalBtcSum.doubleValue() / oBtcBidPrice.doubleValue(), TradeUtils.DEFAULT_PRICE_PRECISION);
				strMessage += "Total UAH = " + MathUtils.toCurrencyStringEx3(oTotalUahSum) + "\r\n";
			}
			strMessage += (aButtons.size() > 0 ? "BUTTONS\r\n" + TelegramTransport.getButtons(aButtons) : StringUtils.EMPTY);
		}
		catch(final Exception e)
		{
			strMessage = e.toString();
		}

		WorkerFactory.getMainWorker().sendSystemMessage(strMessage);
	}

	protected BigDecimal getRateBestBidPrice(final IStockExchange oStockExchange, final Currency oCurrency, final Map<RateInfo, RateAnalysisResult> oRateHash,
													final Map<RateInfo, RateStateShort> oAllRateState) throws Exception
	{
		final RateInfo oRateInfo = new RateInfo(oCurrency, Currency.BTC);
		final RateAnalysisResult oBtcToCurrencyRate = oStockExchange.getLastAnalysisResult().getRateAnalysisResult(oRateInfo);
		if (null != oBtcToCurrencyRate)
			return oBtcToCurrencyRate.getBestBidPrice();
		
		if (null != oAllRateState.get(oRateInfo))
			return oAllRateState.get(oRateInfo).getBidPrice();
		
		final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo);
		if (null != oAllRateState.get(oReverseRateInfo))
			return MathUtils.getBigDecimal(1.0 / oAllRateState.get(oReverseRateInfo).getBidPrice().doubleValue(), TradeUtils.getPricePrecision(oReverseRateInfo));
		
		return null;
	}
}

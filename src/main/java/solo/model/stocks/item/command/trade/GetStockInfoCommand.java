package solo.model.stocks.item.command.trade;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
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
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateStateShort;
import solo.model.stocks.item.Rules;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.AddRuleCommand;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.item.rules.task.trade.SellTaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.telegram.TelegramTransport;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class GetStockInfoCommand extends BaseCommand
{
	public static final String NAME = "info";
	public static final String RATE_PARAMETER = "#rate#";
	
	public GetStockInfoCommand(final String strСommandLine)
	{
		super(strСommandLine, "#type#");
	}
	
	@Override public void execute() throws Exception
	{
		super.execute();
		
		final String strType = getParameter("#type#");
		final boolean bIsSellFreeVolume = strType.contains("sellfreevolume");
		final boolean bIsShowOrders = strType.contains("showorders");
		
		String strMessage = StringUtils.EMPTY;		
		final Map<RateInfo, RateStateShort> oAllRateState = WorkerFactory.getStockSource().getAllRateState();
		
		try
		{		
			final Rules oRules = WorkerFactory.getStockExchange().getRules();
			final StockUserInfo oUserInfo = WorkerFactory.getStockSource().getUserInfo(null);
			
		   	final List<List<String>> aButtons = new LinkedList<List<String>>();
		   	if (bIsShowOrders)
		   	{
				for(final Entry<RateInfo, List<Order>> oOrdersInfo : oUserInfo.getOrders().entrySet())
				{
					for(final Order oOrder : oOrdersInfo.getValue())
					{
						final String strOrderInfo = oOrdersInfo.getKey() + "/" + oOrder.getSide() + "/" + MathUtils.toCurrencyStringEx3(oOrder.getPrice()) + 
														"/" + MathUtils.toCurrencyStringEx3(oOrder.getSum());
						aButtons.add(Arrays.asList(strOrderInfo + " [X]=" + CommandFactory.makeCommandLine(RemoveOrderCommand.class, RemoveOrderCommand.ID_PARAMETER, oOrder.getId())));
					}
				}
		   	}
			
			final Map<Currency, CurrencyAmount> oMoney = ManagerUtils.calculateStockMoney(oUserInfo, oRules);
			for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oMoney.entrySet())
			{
				final BigDecimal nFreeVolum = oCurrencyInfo.getValue().getBalance();
				if (nFreeVolum.compareTo(BigDecimal.valueOf(0.000001)) < 0)
					continue;

				strMessage += oCurrencyInfo.getKey() + "/" + MathUtils.toCurrencyStringEx3(nFreeVolum) + 
						"/" + MathUtils.toCurrencyStringEx3(oCurrencyInfo.getValue().getLocked()) + "\r\n";
				
				if (nFreeVolum.compareTo(BigDecimal.ZERO) == 0 || !bIsSellFreeVolume)
					continue;
				
				for(final Entry<RateInfo, RateStateShort> oShortRateInfo : oAllRateState.entrySet())
				{
					final RateInfo oRateInfo = oShortRateInfo.getKey();
					if (!oRateInfo.getCurrencyFrom().equals(oCurrencyInfo.getKey()))
						continue;
					
					if (TradeUtils.getMinTradeVolume(oRateInfo).compareTo(nFreeVolum) > 0)
						continue;
					
					if (!oUserInfo.getMoney().containsKey(oRateInfo.getCurrencyTo()))
						continue;
					
					final BigDecimal nPrice = oShortRateInfo.getValue().getAskPrice();
					final BigDecimal nSum = nPrice.multiply(nFreeVolum);
					aButtons.add(Arrays.asList("Sell [" + nFreeVolum + "] " + oRateInfo + " [" + nSum + "]=" + CommandFactory.makeCommandLine(AddRuleCommand.class, 
							AddRuleCommand.RULE_TYPE, SellTaskTrade.NAME) + "_" + oRateInfo + "_" + nFreeVolum));
				}
			}
			strMessage += "\r\n";
			
			final Map<RateInfo, RateAnalysisResult> oRateHash = new HashMap<>();	
			BigDecimal oTotalBtcSum = BigDecimal.ZERO;
			final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
			final Map<Currency, BigDecimal> aLocked = new EnumMap<>(Currency.class);
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
			if (null != oBtcBidPrice && oBtcBidPrice.doubleValue() > 0.0)
			{
				final BigDecimal oTotalUahSum = MathUtils.getBigDecimal(oTotalBtcSum.doubleValue() / oBtcBidPrice.doubleValue(), TradeUtils.DEFAULT_PRICE_PRECISION);
				strMessage += "Total UAH = " + MathUtils.toCurrencyStringEx3(oTotalUahSum) + "\r\n";
			}
			
			if (!bIsShowOrders)
				aButtons.add(Arrays.asList("##### SHOW ORDERS #####=" + CommandFactory.makeCommandLine(GetStockInfoCommand.class, "type", "showorders")));
			if (!bIsSellFreeVolume)
				aButtons.add(Arrays.asList("### SELL FREE VOLUME ###=" + CommandFactory.makeCommandLine(GetStockInfoCommand.class, "type", "sellfreevolume")));
			
			strMessage += (!aButtons.isEmpty() ? "BUTTONS\r\n" + TelegramTransport.getButtons(aButtons) : StringUtils.EMPTY);
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

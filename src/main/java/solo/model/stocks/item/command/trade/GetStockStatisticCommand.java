package solo.model.stocks.item.command.trade;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class GetStockStatisticCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "getStatistic";
	final static public String RATE_PARAMETER = "#rate#";
	final static public String DATE_PARAMETER = "#date#";
	
	final protected List<RateInfo> m_aRates;  
	final protected Date m_oDate;  
	
	public GetStockStatisticCommand(final String strСommandLine)
	{
		super(strСommandLine, CommonUtils.mergeParameters(RATE_PARAMETER, DATE_PARAMETER));
		m_aRates = (null != getParameterAsRateInfo(RATE_PARAMETER) ? Arrays.asList(getParameterAsRateInfo(RATE_PARAMETER)) : getStockExchange().getStockSource().getRates()) ;
		m_oDate = (null != getParameterAsDate(DATE_PARAMETER) ? getParameterAsDate(DATE_PARAMETER) : new Date());
	}
	
	public void execute() throws Exception
	{
		super.execute();

		int nPage = 0;
		final int nCount = 1000;
		
		final StockUserInfo oStockUserInfo = getStockExchange().getStockSource().getUserInfo(null);
		
		BigDecimal oTotalUahSum = BigDecimal.ZERO;
		BigDecimal oTotalInTrades = BigDecimal.ZERO;
		BigDecimal oTotalInOrders = BigDecimal.ZERO;
		final DateFormat oFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		String strMessage = oFormat.format(m_oDate) + "\r\n";
		for(final RateInfo  oRateInfo : m_aRates)
		{
			final List<Order> oTrades = getStockExchange().getStockSource().getTrades(oRateInfo, nPage, nCount);
			
			BigDecimal oTotalInRateTrades = BigDecimal.ZERO;
			for(final Order oOrder : oTrades)
			{
				if (m_oDate.before(oOrder.getCreated()))
					continue;
					
				if (oOrder.getSide().equals(OrderSide.BUY))
					oTotalInRateTrades = oTotalInRateTrades.add(oOrder.getSum().negate());
				else
					oTotalInRateTrades = oTotalInRateTrades.add(oOrder.getSum());
			}
			
			BigDecimal oTotalInRateOrders = BigDecimal.ZERO;
			for(final Order oOrder : oStockUserInfo.getOrders(oRateInfo))
				oTotalInRateOrders = oTotalInRateOrders.add(oOrder.getSum());

			BigDecimal oTotalInRate = oTotalInRateTrades.add(oTotalInRateOrders);
			strMessage += "Total [" + oRateInfo.getCurrencyFrom() + "] = " + MathUtils.toCurrencyString(oTotalInRate) + " / " + MathUtils.toCurrencyString(oTotalInRateTrades) + 
							" / " + MathUtils.toCurrencyString(oTotalInRateOrders) + "\r\n";

			oTotalInTrades = oTotalInTrades.add(oTotalInRateTrades);
			oTotalInOrders = oTotalInOrders.add(oTotalInRateOrders);
			oTotalUahSum = oTotalUahSum.add(oTotalInRateOrders);
		}

		BigDecimal oTotal = oTotalInTrades.add(oTotalInOrders);
		strMessage += "Total = " + MathUtils.toCurrencyString(oTotal) + " / " + MathUtils.toCurrencyString(oTotalInTrades) + " / " + MathUtils.toCurrencyString(oTotalInOrders) + "\r\n";
		
		for(final Entry<Currency, CurrencyAmount> oCurrencyAmount : oStockUserInfo.getMoney().entrySet())
		{
			final RateInfo oRateInfo = new RateInfo(oCurrencyAmount.getKey(), Currency.UAH);
			if (oCurrencyAmount.getKey().equals(Currency.UAH))
			{
				oTotalUahSum = oTotalUahSum.add(oCurrencyAmount.getValue().getBalance());
				oTotalUahSum = oTotalUahSum.add(oCurrencyAmount.getValue().getLocked());
			}
			else
			{
				if (null == getStockExchange().getHistory().getLastAnalysisResult().getRateAnalysisResult(oRateInfo))
					continue;
				
				final BigDecimal oBidPrice = getStockExchange().getHistory().getLastAnalysisResult().getRateAnalysisResult(oRateInfo).getBidsAnalysisResult().getBestPrice();
				final BigDecimal oVolume = oCurrencyAmount.getValue().getBalance();
				final BigDecimal oSum = oVolume.multiply(oBidPrice);
				oTotalUahSum = oTotalUahSum.add(oSum);
			}
			
		}
		strMessage += "Total UAH = " + MathUtils.toCurrencyString(oTotalUahSum) + "\r\n";
		
		sendMessage(strMessage);
	}
}

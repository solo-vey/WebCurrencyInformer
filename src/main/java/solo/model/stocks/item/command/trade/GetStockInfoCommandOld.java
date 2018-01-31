package solo.model.stocks.item.command.trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class GetStockInfoCommandOld extends BaseCommand
{
	final static public String NAME = "getInfo";
	final static public String RATE_PARAMETER = "#rate#";
	
	final protected RateInfo m_oRateInfo;  
	
	public GetStockInfoCommandOld(final String strСommandLine)
	{
		super(strСommandLine, RATE_PARAMETER);
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final StockUserInfo oUserInfo = WorkerFactory.getStockExchange().getStockSource().getUserInfo(m_oRateInfo);
		
		String strMessage = StringUtils.EMPTY;
		for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oUserInfo.getMoney().entrySet())
		{
			strMessage += oCurrencyInfo.getKey() + "/" + MathUtils.toCurrencyStringEx3(oCurrencyInfo.getValue().getBalance()) + 
							(oCurrencyInfo.getValue().getLocked().compareTo(BigDecimal.ZERO) != 0 ? "/" + MathUtils.toCurrencyStringEx3(oCurrencyInfo.getValue().getLocked()) : StringUtils.EMPTY)
							+ "\r\n";
		}

		for(final Entry<RateInfo, List<Order>> oOrdersInfo : oUserInfo.getOrders().entrySet())
		{
			for(final Order oOrder : oOrdersInfo.getValue())
				strMessage += oOrdersInfo.getKey() + "/" + oOrder.getInfo() + "\r\n";
		}
		
		final RateInfo oRateEthUahInfo = new RateInfo(Currency.ETH, Currency.UAH);
		final RateAnalysisResult oEthUahRateAnalysisResult = WorkerFactory.getStockExchange().getLastAnalysisResult().getRateAnalysisResult(oRateEthUahInfo);
		final BigDecimal oEthUahPrice = (null != oEthUahRateAnalysisResult ? oEthUahRateAnalysisResult.getBidsAnalysisResult().getBestPrice() : BigDecimal.ZERO);
		BigDecimal oTotalUahSum = BigDecimal.ZERO;
		for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oUserInfo.getMoney().entrySet())
		{
			final RateInfo oRateInfo = new RateInfo(oCurrencyInfo.getKey(), Currency.UAH);
			if (oCurrencyInfo.getKey().equals(Currency.UAH))
				oTotalUahSum = oTotalUahSum.add(oCurrencyInfo.getValue().getBalance());
			else
			{
				if (null == WorkerFactory.getStockExchange().getLastAnalysisResult().getRateAnalysisResult(oRateInfo))
				{
					if (null == oEthUahPrice)
						continue;
					
					final RateInfo oEthRateInfo = new RateInfo(Currency.ETH, oCurrencyInfo.getKey());
					if (null == WorkerFactory.getStockExchange().getLastAnalysisResult().getRateAnalysisResult(oEthRateInfo))
						continue;

					final BigDecimal oEthBidPrice = WorkerFactory.getStockExchange().getLastAnalysisResult().getRateAnalysisResult(oEthRateInfo).getBidsAnalysisResult().getBestPrice();
					final BigDecimal oCrossBidPrice = MathUtils.getBigDecimal(oEthUahPrice.doubleValue() / oEthBidPrice.doubleValue(), TradeUtils.getVolumePrecision(oRateInfo));

					final BigDecimal oVolume = oCurrencyInfo.getValue().getBalance();
					final BigDecimal oSum = oVolume.multiply(oCrossBidPrice);
					oTotalUahSum = oTotalUahSum.add(oSum);
				}
				else
				{
					final BigDecimal oBidPrice = WorkerFactory.getStockExchange().getLastAnalysisResult().getRateAnalysisResult(oRateInfo).getBidsAnalysisResult().getBestPrice();
					final BigDecimal oVolume = oCurrencyInfo.getValue().getBalance();
					final BigDecimal oSum = oVolume.multiply(oBidPrice);
					oTotalUahSum = oTotalUahSum.add(oSum);
				}
			}
		}

		for(final Entry<RateInfo, List<Order>> oOrdersInfo : oUserInfo.getOrders().entrySet())
		{
			for(final Order oOrder : oOrdersInfo.getValue())
			{
				if (oOrdersInfo.getKey().getCurrencyTo().equals(Currency.UAH))
					oTotalUahSum = oTotalUahSum.add(oOrder.getSum());
				else
				{
					final RateInfo oEthRateInfo = new RateInfo(Currency.ETH, oOrdersInfo.getKey().getCurrencyTo());
					if (null == WorkerFactory.getStockExchange().getLastAnalysisResult().getRateAnalysisResult(oEthRateInfo))
						continue;
					
					final BigDecimal oEthBidPrice = WorkerFactory.getStockExchange().getLastAnalysisResult().getRateAnalysisResult(oEthRateInfo).getBidsAnalysisResult().getBestPrice();
					final BigDecimal oCrossBidPrice = MathUtils.getBigDecimal(oEthUahPrice.doubleValue() / oEthBidPrice.doubleValue(), TradeUtils.DEFAULT_VOLUME_PRECISION);

					final BigDecimal oSum = oOrder.getSum().multiply(oCrossBidPrice);
					oTotalUahSum = oTotalUahSum.add(oSum);
				}
			}
		}
		
		strMessage += "Total UAH = " + MathUtils.toCurrencyStringEx3(oTotalUahSum) + "\r\n";

		WorkerFactory.getMainWorker().sendMessage(strMessage);
	}
}

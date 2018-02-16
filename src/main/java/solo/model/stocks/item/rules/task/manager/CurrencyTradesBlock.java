package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.trade.TaskTrade;
import solo.model.stocks.item.rules.task.trade.TradeInfo;
import solo.model.stocks.worker.WorkerFactory;

public class CurrencyTradesBlock implements Serializable
{
	private static final long serialVersionUID = 7340981410262177314L;
	
	public Map<Currency, TradesBlock> m_oCurrencyTrades = new HashMap<Currency, TradesBlock>();
	
	public void addTrade(final TaskTrade oTaskTrade)
	{
		addTrade(oTaskTrade.getTradeInfo());
	}
	
	public void addTrade(final TradeInfo oTradeInfo)
	{
		final Currency oCurrency = oTradeInfo.getRateInfo().getCurrencyTo();
		if (!m_oCurrencyTrades.containsKey(oCurrency))
			m_oCurrencyTrades.put(oCurrency, new TradesBlock());
		m_oCurrencyTrades.get(oCurrency).addTrade(oTradeInfo);
		
		final RateInfo oRateInfo = new RateInfo(oCurrency, Currency.BTC);
		final RateAnalysisResult oBtcToCurrencyRate = WorkerFactory.getStockExchange().getLastAnalysisResult().getRateAnalysisResult(oRateInfo);
		if (null != oBtcToCurrencyRate)
		{
			BigDecimal nSpendSumInCurrency = oTradeInfo.getSpendSum();
			final BigDecimal nNeedSellVolume = oTradeInfo.getNeedSellVolume();
			if (nNeedSellVolume.compareTo(BigDecimal.ZERO) > 0)
			{
				final BigDecimal nNeedSellSum = nNeedSellVolume.multiply(oTradeInfo.getAveragedBoughPrice());
				nSpendSumInCurrency = nSpendSumInCurrency.add(nNeedSellSum.negate());
			}
			
			final BigDecimal oBtcBidPrice = oBtcToCurrencyRate.getBestBidPrice();
			final BigDecimal nSpendSum = nSpendSumInCurrency.multiply(oBtcBidPrice);
			final BigDecimal nReceivedSum = oTradeInfo.getReceivedSum().multiply(oBtcBidPrice);
			final TradeInfo oBtcTradeInfo = new TradeInfo(oRateInfo, -1);
			oBtcTradeInfo.addBuy(nSpendSum, BigDecimal.ZERO);
			oBtcTradeInfo.addSell(nReceivedSum, BigDecimal.ZERO);
			
			if (!m_oCurrencyTrades.containsKey(Currency.BTC))
				m_oCurrencyTrades.put(Currency.BTC, new TradesBlock());
			m_oCurrencyTrades.get(Currency.BTC).addTrade(oBtcTradeInfo);
		}
	}
	
	@Override public String toString()
	{
		String strResult = StringUtils.EMPTY;
		for(final Entry<Currency, TradesBlock> oTradesInfo : m_oCurrencyTrades.entrySet())
			strResult += oTradesInfo.getKey() + " : " + oTradesInfo.getValue() + "\r\n";
		return strResult;
	}
}

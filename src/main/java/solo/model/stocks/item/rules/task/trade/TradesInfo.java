package solo.model.stocks.item.rules.task.trade;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.utils.MathUtils;

public class TradesInfo extends BaseObject implements Serializable
{
	private static final long serialVersionUID = -7601846839785106296L;
	
	protected RateInfo m_oRateInfo;
	protected String m_strHistory = StringUtils.EMPTY;
	protected List<TradeInfo> m_aTradeHistory = new LinkedList<TradeInfo>();
	
	protected BigDecimal m_nSum = BigDecimal.ZERO;
	protected BigDecimal m_nLockedSum = BigDecimal.ZERO;
	protected BigDecimal m_nSumToSell = BigDecimal.ZERO;
	protected BigDecimal m_nVolume = BigDecimal.ZERO;
	protected BigDecimal m_nLockedVolume = BigDecimal.ZERO;
	protected BigDecimal m_nBuySum = BigDecimal.ZERO;

	protected BigDecimal m_nReceivedSum = BigDecimal.ZERO;
	protected BigDecimal m_nSpendSum = BigDecimal.ZERO;
	
	protected BigDecimal m_nBuyVolume = BigDecimal.ZERO;
	protected BigDecimal m_nSoldVolume = BigDecimal.ZERO;
	
	public TradesInfo(final RateInfo oRateInfo)
	{
		m_oRateInfo = oRateInfo;
	}
	
	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}
	
	public void setRateInfo(final RateInfo oRateInfo)
	{
		m_oRateInfo = oRateInfo;
	}

	public BigDecimal getDelta()
	{
		return m_nReceivedSum.add(m_nSpendSum.negate());
	}
	
	public String getHistory()
	{
		return m_strHistory;
	}
	
	public BigDecimal getSpendSum()
	{
		return m_nSpendSum;
	}
	
	public BigDecimal getReceivedSum()
	{
		return m_nReceivedSum;
	}
	
	public BigDecimal getSum()
	{
		return m_nSum;
	}
	
	public BigDecimal getLockedSum()
	{
		return m_nLockedSum;
	}
	
	public BigDecimal getFreeSum()
	{
		return m_nSum.add(m_nLockedSum.negate());
	}
	
	public BigDecimal getSumToSell()
	{
		return m_nSumToSell;
	}
	
	public void setSum(final BigDecimal nSum, final Integer nMaxTrades)
	{
		m_nSum = nSum;
		m_nBuySum = MathUtils.getRoundedBigDecimal(nSum.doubleValue() / nMaxTrades, TradeUtils.getVolumePrecision(m_oRateInfo)); 
	}
	
	public BigDecimal getBuySum()
	{
		return m_nBuySum;
	}
	
	public BigDecimal getSoldVolume()
	{
		return m_nSoldVolume;
	}
	
	public BigDecimal getBuyVolume()
	{
		return m_nBuyVolume;
	}
	
	public BigDecimal getVolume()
	{
		return m_nVolume;
	}
	
	public BigDecimal getLockedVolume()
	{
		return m_nLockedVolume;
	}
	
	public BigDecimal getFreeVolume()
	{
		return m_nVolume.add(m_nLockedVolume.negate());
	}
	
	public Integer getTradeCount()
	{
		return m_aTradeHistory.size();
	}
	
	public void addBuy(BigDecimal nSpendSum, BigDecimal nBuyVolume)
	{
		if (nSpendSum.compareTo(BigDecimal.ZERO) == 0 && nBuyVolume.compareTo(BigDecimal.ZERO) == 0)
			return;
		
		m_nSpendSum = m_nSpendSum.add(nSpendSum);
		m_nSum = m_nSum.add(nSpendSum.negate());

		m_nBuyVolume = m_nBuyVolume.add(nBuyVolume);
		m_nVolume = m_nVolume.add(nBuyVolume);
		
		addToHistory("Buy : " + MathUtils.toCurrencyString(nSpendSum) + " / " + MathUtils.toCurrencyStringEx(nBuyVolume)); 
	}
	
	public void addSell(BigDecimal nReceivedSum, BigDecimal nSoldVolume)
	{
		if (nReceivedSum.compareTo(BigDecimal.ZERO) == 0 && nSoldVolume.compareTo(BigDecimal.ZERO) == 0)
			return;

		m_nReceivedSum = m_nReceivedSum.add(nReceivedSum);
		m_nSum = m_nSum.add(nReceivedSum);

		m_nSoldVolume = m_nSoldVolume.add(nSoldVolume);
		m_nVolume = m_nVolume.add(nSoldVolume.negate());
		
		addToHistory("Sell : " + MathUtils.toCurrencyString(nReceivedSum) + " / " + MathUtils.toCurrencyStringEx(nSoldVolume)); 
	}
	
	public void tradeStart(final TaskTrade oTaskTrade)
	{
		m_aTradeHistory.add(oTaskTrade.getTradeInfo());
	}
	
	public void buyDone(final TaskTrade oTaskTrade)
	{
	}
	
	public void tradeDone(final TaskTrade oTaskTrade)
	{
	}
	
	protected void addToHistory(final String strMessage)
	{
		final DateFormat oDateFormat = new SimpleDateFormat("dd.MM HH:mm:ss");
		m_strHistory += oDateFormat.format(new Date()) + " " + strMessage + "\r\n";
	}
	
	protected void clearHistory()
	{
		m_strHistory = StringUtils.EMPTY;
	}
	
	public void updateOrderInfo(final List<ITradeTask> aTaskTrades)
	{
		m_nLockedSum = BigDecimal.ZERO;
		m_nLockedVolume = BigDecimal.ZERO;
		m_nSumToSell = BigDecimal.ZERO;
		for(final ITradeTask oTaskTrade : aTaskTrades)
		{
			final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
			if (oOrder.isNull() || oOrder.isError() || oOrder.isCanceled() || oOrder.isDone())
				continue;
			
			if (oOrder.getSide().equals(OrderSide.BUY))
				m_nLockedSum = m_nLockedSum.add(oOrder.getSum());

			if (oOrder.getSide().equals(OrderSide.SELL))
			{
				m_nLockedVolume = m_nLockedVolume.add(oOrder.getVolume());
				m_nSumToSell = m_nSumToSell.add(oOrder.getSum());
			}
		}
	}
	
	public String getInfo()
	{
		final BigDecimal nReceiveAndSellSum = getReceivedSum().add(getSumToSell());
		final BigDecimal nDelta = nReceiveAndSellSum.add(getSpendSum().negate());
		return  "Count: " + getTradeCount() + "\r\n" + 
				"Money: " + MathUtils.toCurrencyStringEx2(getSum()) + "/" + MathUtils.toCurrencyStringEx2(getLockedSum()) + "/" + MathUtils.toCurrencyStringEx2(getFreeSum()) + "/" + MathUtils.toCurrencyStringEx2(getSumToSell()) + "\r\n" + 
				"Volume:" + MathUtils.toCurrencyStringEx2(getVolume()) + "/" + MathUtils.toCurrencyStringEx2(getLockedVolume()) +  "/" + MathUtils.toCurrencyStringEx2(getFreeVolume()) + "\r\n" + 
				"Trades: " + MathUtils.toCurrencyStringEx2(nReceiveAndSellSum) + "-" + MathUtils.toCurrencyStringEx2(getSpendSum()) + "=" + MathUtils.toCurrencyStringEx2(nDelta);
	}
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		String strResult = StringUtils.EMPTY;
		strResult += m_oRateInfo + "\r\n";
		
		strResult += "Sum: " + MathUtils.toCurrencyStringEx2(getSum()) + "\r\n";
		strResult += "LockedSum: " + MathUtils.toCurrencyStringEx2(getLockedSum()) + "\r\n";
		strResult += "SumToSell: " + MathUtils.toCurrencyStringEx2(getSumToSell()) + "\r\n";
		strResult += "Volume: " + MathUtils.toCurrencyStringEx2(getVolume()) + "\r\n";
		strResult += "LockedVolume: " + MathUtils.toCurrencyStringEx2(getLockedVolume()) + "\r\n";
		strResult += "BuySum: " + MathUtils.toCurrencyStringEx2(getBuySum()) + "\r\n";
		strResult += "ReceivedSum: " + MathUtils.toCurrencyStringEx2(getReceivedSum()) + "\r\n";
		strResult += "SpendSum: " + MathUtils.toCurrencyStringEx2(getSpendSum()) + "\r\n";
		strResult += "BuyVolume: " + MathUtils.toCurrencyStringEx2(getBuyVolume()) + "\r\n";
		strResult += "SoldVolume: " + MathUtils.toCurrencyStringEx2(getSoldVolume()) + "\r\n";
		
		for(final TradeInfo oTradeInfo : m_aTradeHistory)
			strResult += "Trade: " + MathUtils.toCurrencyStringEx2(oTradeInfo.getReceivedSum()) + "-" + MathUtils.toCurrencyStringEx2(oTradeInfo.getSpendSum()) + "=" + MathUtils.toCurrencyStringEx2(oTradeInfo.getDelta()) + "\r\n";

		return strResult;
	}
}

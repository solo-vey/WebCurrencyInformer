package solo.model.stocks.item;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import solo.model.currency.Currency;
import solo.model.stocks.BaseObject;
import solo.utils.MathUtils;

public class RateStateShort extends BaseObject
{
	final protected RateInfo m_oRateInfo;
	final protected BigDecimal m_nBidPrice;
	final protected BigDecimal m_nAskPrice;
	final protected BigDecimal m_nVolume;
	
	public RateStateShort(final RateInfo oRateInfo, final BigDecimal nBidPrice, final BigDecimal nAskPrice, final BigDecimal nVolume)
	{
		m_oRateInfo = oRateInfo;
		m_nBidPrice = nBidPrice;
		m_nAskPrice = nAskPrice;
		m_nVolume = nVolume;
	}

	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}
	
	public BigDecimal getAskPrice()
	{
		return m_nAskPrice;
	}
	
	public BigDecimal getBidPrice()
	{
		return m_nBidPrice;
	}
	
	public BigDecimal getVolume()
	{
		return m_nVolume;
	}
	
	@SuppressWarnings("unchecked")
	static public RateStateShort getFromData(final Entry<String, Object> oRateData)
	{
		try
		{
			final String[] aRateParts = oRateData.getKey().split("_");
			final Currency oCurrencyFrom = Currency.valueOf(aRateParts[0]);
			final Currency oCurrencyTo = Currency.valueOf(aRateParts[1]);
			final RateInfo oRateInfo = new RateInfo(oCurrencyFrom, oCurrencyTo);
			
			final Map<String, Object> oInfoData = (Map<String, Object>) oRateData.getValue();
			final BigDecimal nBidPrice = MathUtils.fromString(oInfoData.get("buy_price").toString());
			final BigDecimal nAskPrice = MathUtils.fromString(oInfoData.get("sell_price").toString());
			final BigDecimal nVolume = MathUtils.fromString(oInfoData.get("vol").toString());
			
			return new RateStateShort(oRateInfo, nBidPrice, nAskPrice, nVolume);
		}
		catch(final Exception e) {}
		
		return null;
	}
}

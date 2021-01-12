package solo.model.stocks.item;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import solo.model.currency.Currency;
import solo.model.stocks.BaseObject;
import solo.utils.MathUtils;

public class RateStateShort extends BaseObject
{
	protected final RateInfo m_oRateInfo;
	protected final BigDecimal m_nBidPrice;
	protected final BigDecimal m_nAskPrice;
	protected final BigDecimal m_nVolume;
	
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
	public static RateStateShort getFromData(final Entry<String, Object> oRateData)
	{
		return getFromData((Map<String, Object>) oRateData.getValue(), oRateData.getKey(), "_", "buy_price", "sell_price", "vol");
	}
	
	public static RateStateShort getFromData(final Map<String, Object> oInfoData, final String strRate, final String strSpliter, final String strBuyPriceKey, final String strSellPriceKey, final String strVolumeKey)
	{
		try
		{
			final String[] aRateParts = strRate.split(strSpliter);
			final Currency oCurrencyFrom = Currency.valueOf(aRateParts[0]);
			final Currency oCurrencyTo = Currency.valueOf(aRateParts[1]);
			final RateInfo oRateInfo = new RateInfo(oCurrencyFrom, oCurrencyTo);
			
			final BigDecimal nBidPrice = MathUtils.fromString(oInfoData.get(strBuyPriceKey).toString());
			final BigDecimal nAskPrice = MathUtils.fromString(oInfoData.get(strSellPriceKey).toString());
			final BigDecimal nVolume = MathUtils.fromString(oInfoData.get(strVolumeKey).toString());
			
			return new RateStateShort(oRateInfo, nBidPrice, nAskPrice, nVolume);
		}
		catch(final Exception e) {}
		
		return null;
	}
}

package solo.model.stocks.item.rules.task.strategy.manager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.ResourceUtils;

public class BaseManagerStrategy implements IManagerStrategy
{
	private static final long serialVersionUID = -1074235543708296492L;
	
	protected Integer m_nLastProfitabilityCheckPeriod = -1;
	protected Integer m_nLastUnProfitabilityCheckPeriod = -1;
	
	final protected BigDecimal m_nMinAverageUnprofitabilityPercent;
	final protected BigDecimal m_nMinHourUnprofitabilityPercent;
	final protected BigDecimal m_nMinAverageProfitabilityPercent;
	
	public BaseManagerStrategy(final IStockExchange oStockExchange)
	{
		m_nMinAverageUnprofitabilityPercent = ResourceUtils.getBigDecimalFromResource("stock.min.average.unprofitability_percent", oStockExchange.getStockProperties(), BigDecimal.ZERO);
		m_nMinHourUnprofitabilityPercent = ResourceUtils.getBigDecimalFromResource("stock.min.hour.unprofitability_percent", oStockExchange.getStockProperties(), BigDecimal.ZERO);
		m_nMinAverageProfitabilityPercent = ResourceUtils.getBigDecimalFromResource("stock.min.hour.profitability_percent", oStockExchange.getStockProperties(), BigDecimal.ONE);
	}

	@Override public Map<BigDecimal, RateInfo> getMoreProfitabilityRates()
	{
		if (!getNeedCheckProfitability())
			return new HashMap<BigDecimal, RateInfo>();
		
		final TreeMap<BigDecimal, RateInfo> oRatePercents = new TreeMap<BigDecimal, RateInfo>(Collections.reverseOrder());
		for(final RateInfo oRateInfo : WorkerFactory.getStockSource().getRates())
		{
			final BigDecimal nAverageRateProfitabilityPercent = ManagerUtils.getAverageRateProfitabilityPercent(oRateInfo); 
			if (nAverageRateProfitabilityPercent.compareTo(m_nMinAverageProfitabilityPercent) >= 0)
				oRatePercents.put(nAverageRateProfitabilityPercent, oRateInfo);
			
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo);
			final BigDecimal nAverageReverseRateProfitabilityPercent = ManagerUtils.getAverageRateProfitabilityPercent(oReverseRateInfo); 
			if (nAverageReverseRateProfitabilityPercent.compareTo(m_nMinAverageProfitabilityPercent) >= 0)
				oRatePercents.put(nAverageReverseRateProfitabilityPercent, oReverseRateInfo);
		}
		
		return oRatePercents;
	}
	
	@Override public Map<BigDecimal, RateInfo> getUnProfitabilityRates()
	{
		if (!getNeedCheckUnProfitability())
			return new HashMap<BigDecimal, RateInfo>();
		
		final TreeMap<BigDecimal, RateInfo> oUnProfitabilityRatesPercents = new TreeMap<BigDecimal, RateInfo>();
		for(final RateInfo oRateInfo : WorkerFactory.getStockSource().getRates())
		{
			final BigDecimal nAverageRateProfitabilityPercent = ManagerUtils.getAverageRateProfitabilityPercent(oRateInfo); 
			final BigDecimal nMinRateHourProfitabilityPercent = ManagerUtils.getMinRateHourProfitabilityPercent(oRateInfo); 			
			if (nAverageRateProfitabilityPercent.compareTo(m_nMinAverageUnprofitabilityPercent) < 0 ||
				nMinRateHourProfitabilityPercent.compareTo(m_nMinHourUnprofitabilityPercent) < 0)
					oUnProfitabilityRatesPercents.put(nAverageRateProfitabilityPercent, oRateInfo);
			
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo);
			final BigDecimal nAverageReverseRateProfitabilityPercent = ManagerUtils.getAverageRateProfitabilityPercent(oReverseRateInfo); 
			final BigDecimal nMinRateHourReverseProfitabilityPercent = ManagerUtils.getMinRateHourProfitabilityPercent(oReverseRateInfo); 			
			if (nAverageReverseRateProfitabilityPercent.compareTo(m_nMinAverageUnprofitabilityPercent) < 0 ||
				nMinRateHourReverseProfitabilityPercent.compareTo(m_nMinHourUnprofitabilityPercent) < 0)
					oUnProfitabilityRatesPercents.put(nAverageReverseRateProfitabilityPercent, oReverseRateInfo);
		}
	
		return oUnProfitabilityRatesPercents;
	}	
	
	protected boolean getNeedCheckProfitability()
	{
		final Integer nLastProfitabilityCheckPeriod = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (m_nLastProfitabilityCheckPeriod.equals(nLastProfitabilityCheckPeriod))
			return false;
		
		m_nLastProfitabilityCheckPeriod = nLastProfitabilityCheckPeriod;
		return true;
	}
	
	protected boolean getNeedCheckUnProfitability()
	{
		final Integer nLastUnProfitabilityCheckPeriod = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (m_nLastUnProfitabilityCheckPeriod.equals(nLastUnProfitabilityCheckPeriod))
			return false;
		
		m_nLastUnProfitabilityCheckPeriod = nLastUnProfitabilityCheckPeriod;
		return true;
	}
}

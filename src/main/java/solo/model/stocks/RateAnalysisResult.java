package solo.model.stocks;

import java.util.List;

public class RateAnalysisResult extends BaseObject
{
	final protected RateInfo m_oRateInfo;
	final protected OrderAnalysisResult m_oAsksAnalysisResult;
	final protected OrderAnalysisResult m_oBidsAnalysisResult;
	final protected OrderAnalysisResult m_oTradesAnalysisResult;
	
	public RateAnalysisResult(final StockRateStates oStockRateStates, final RateInfo oRateInfo, final IStockExchange oStockExchange) throws Exception
	{
		m_oRateInfo = oRateInfo;
    	final List<Order> oAsksOrders = oStockRateStates.getRate(oRateInfo).getAsksOrders();
    	final List<Order> oBidsOrders = oStockRateStates.getRate(oRateInfo).getBidsOrders();
    	final List<Order> oTrades = oStockRateStates.getRate(oRateInfo).getTrades();
    	
    	final double nCurrencyVolume = oStockExchange.getStockCurrencyVolume(oRateInfo.getCurrencyTo()).getVolume().doubleValue();
    	
    	m_oAsksAnalysisResult = new OrderAnalysisResult(oAsksOrders, nCurrencyVolume, nCurrencyVolume);
    	m_oBidsAnalysisResult = new OrderAnalysisResult(oBidsOrders, nCurrencyVolume, nCurrencyVolume);
    	m_oTradesAnalysisResult = new OrderAnalysisResult(oTrades, nCurrencyVolume, nCurrencyVolume);
	}

	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}

	public OrderAnalysisResult getAsksAnalysisResult()
	{
		return m_oAsksAnalysisResult;
	}

	public OrderAnalysisResult getBidsAnalysisResult()
	{
		return m_oBidsAnalysisResult;
	}

	public OrderAnalysisResult getTradesAnalysisResult()
	{
		return m_oTradesAnalysisResult;
	}
}

package solo.model.stocks.item.command;

import java.util.List;

import solo.model.currency.Currency;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.oracle.RateForecast;
import solo.model.stocks.oracle.RatesForecast;
import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class GetRateInfoCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "getRateInfo";
	final static public String TEMPLATE = NAME + "_%s";

	final protected RateInfo m_oRateInfo;
	
	public GetRateInfoCommand(final String strRateInfo)
	{
		super(strRateInfo);
		final String strCurrencyFrom = strRateInfo.toUpperCase();
		m_oRateInfo = new RateInfo(Currency.valueOf(strCurrencyFrom), Currency.UAH); 
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final IStockExchange oStockExchange = StockExchangeFactory.getDefault(); 
    	final StateAnalysisResult oStateAnalysisResult = oStockExchange.getHistory().getLastAnalysisResult();
		final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
		String strMessage = "Sell : " + MathUtils.toCurrencyString(oAnalysisResult.getAsksAnalysisResult().getBestPrice()) + 
							" / " + MathUtils.toCurrencyString(oAnalysisResult.getAsksAnalysisResult().getAverageAllSumPrice()) + "\r\n";   
		strMessage += "Buy : " + MathUtils.toCurrencyString(oAnalysisResult.getBidsAnalysisResult().getBestPrice()) + 
							" / " + MathUtils.toCurrencyString(oAnalysisResult.getBidsAnalysisResult().getAverageAllSumPrice()) + "\r\n";   
		strMessage += "Trades : " + MathUtils.toCurrencyString(oAnalysisResult.getTradesAnalysisResult().getBestPrice()) + 
							" / " + MathUtils.toCurrencyString(oAnalysisResult.getTradesAnalysisResult().getAverageAllSumPrice()) + "\r\n";   
		
		final List<RatesForecast> oForecasts = oStockExchange.getHistory().getFuture();
		if (null != oForecasts && oForecasts.size() > 0)
		{
			final RatesForecast oForecast = oForecasts.get(0);
			final RateForecast oRateForecast = oForecast.getForecust(m_oRateInfo);
			strMessage += "Forecast : " + MathUtils.toCurrencyString(oRateForecast.getPrice());
		}
		
		final ICommand oCommand = new SendMessageCommand(TransportFactory.getTransport(TelegramTransport.NAME), strMessage);
		WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oCommand);
	}
}

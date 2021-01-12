package solo.model.stocks.item.command.system;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.rule.AddControlerCommand;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.telegram.TelegramTransport;

/** Формат комманды 
 */
public class GetRateChartCommand extends BaseCommand
{
	public static final String NAME = "chart";
	public static final String RATE_PARAMETER = "#rate#";

	protected final RateInfo m_oRateInfo;
	
	public GetRateChartCommand(final String strRateInfo)
	{
		super(strRateInfo, RATE_PARAMETER);
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	@Override public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = WorkerFactory.getStockExchange(); 
		final Candlestick oCandlestick = oStockExchange.getStockCandlestick().get(m_oRateInfo);
    	final String strFileName = oCandlestick.makeChartImage(50);
    	
    	final StateAnalysisResult oStateAnalysisResult = oStockExchange.getLastAnalysisResult();
    	final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo);
    	String strMessage = GetRateInfoCommand.getRateData(m_oRateInfo, oAnalysisResult);
    	
		final BigDecimal nAverageRateProfitabilityPercent = ManagerUtils.getAverageRateProfitabilityPercent(m_oRateInfo); 
		final BigDecimal nMinRateHourProfitabilityPercent = ManagerUtils.getMinRateHourProfitabilityPercent(m_oRateInfo); 
		strMessage += "\r\nAv [" + nAverageRateProfitabilityPercent + " %] Min [" + nMinRateHourProfitabilityPercent + "%]";
   	
    	final List<List<String>> aButtons = new LinkedList<List<String>>();
    	List<String> aLine = new LinkedList<String>();
    	for(final RateInfo oRateInfo : WorkerFactory.getStockSource().getRates())
    	{
    		if (oRateInfo.equals(m_oRateInfo))
    			continue;
    		
    		if (aLine.size() == 4)
    		{
    			aButtons.add(aLine);
    			aLine = new LinkedList<String>();
    		}
    		
    		aLine.add(oRateInfo.toString().toUpperCase() + "=" + NAME + "_" + oRateInfo);
    	}
		if (aLine.size() > 0)
			aButtons.add(aLine);
		
		aButtons.add(Arrays.asList("Rules [" + m_oRateInfo + "]=/rules_rate:" + m_oRateInfo));
		
		final BigDecimal nSum = TradeUtils.getRoundedPrice(m_oRateInfo, TradeUtils.getMinTradeSum(m_oRateInfo).multiply(BigDecimal.valueOf(2)));
		if (nSum.compareTo(BigDecimal.ZERO) > 0)
			aButtons.add(Arrays.asList("Create controler [" + m_oRateInfo + "][" + nSum + "]=" + 
					CommandFactory.makeCommandLine(AddControlerCommand.class, AddControlerCommand.RATE_PARAMETER, m_oRateInfo,
					AddControlerCommand.SUM_PARAMETER, nSum)));
	
		final RateInfo oReverseRateInfo = RateInfo.getReverseRate(m_oRateInfo);
		final BigDecimal nReverseSum = TradeUtils.getRoundedPrice(oReverseRateInfo, TradeUtils.getMinTradeSum(oReverseRateInfo).multiply(BigDecimal.valueOf(2)));
		if (nReverseSum.compareTo(BigDecimal.ZERO) > 0)
			aButtons.add(Arrays.asList("Create controler [" + oReverseRateInfo + "][" + nReverseSum + "]=" + 
					CommandFactory.makeCommandLine(AddControlerCommand.class, AddControlerCommand.RATE_PARAMETER, oReverseRateInfo,
					AddControlerCommand.SUM_PARAMETER, nReverseSum)));
		
		WorkerFactory.getTransport().sendPhoto(new File(strFileName), strMessage + 
    			"BUTTONS\r\n" + TelegramTransport.getButtons(aButtons));
	}
}

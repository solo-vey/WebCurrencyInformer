package solo.model.stocks.item.rules.notify;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.analyse.JapanCandle;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.rule.GetRulesCommand;
import solo.model.stocks.item.command.base.ICommand;
import solo.model.stocks.item.command.system.SendMessageCommand;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;

public class EventTrendTrace extends EventBase
{
	private static final long serialVersionUID = -1423107368298871795L;
	
	protected List<JapanCandle> m_oHistory = new LinkedList<JapanCandle>();
	protected Integer m_nCandleDurationMinutes = 5;
	
	public EventTrendTrace(final RateInfo oRateInfo, final String strPriceInfo)
	{
		super(oRateInfo, strPriceInfo);
		m_oHistory.add(new JapanCandle());
	}

	@Override public String getType()
	{
		return "TrendTrace";   
	}
	
	public String getInfo(final Integer nRuleID)
	{
		final JapanCandle oLastCandle = m_oHistory.get(m_oHistory.size() - 1);
		return getType() + "/" + m_oRateInfo + "/" + 
			MathUtils.toCurrencyString(getMinPrice()) + "-" + MathUtils.toCurrencyString(getMaxPrice()) + "\r\n" +
			oLastCandle.getType() + "/" + MathUtils.toCurrencyString(oLastCandle.getStart()) + "/" + MathUtils.toCurrencyString(oLastCandle.getMax()) + 
			"/" + MathUtils.toCurrencyString(oLastCandle.getMin()) + "/" + MathUtils.toCurrencyString(oLastCandle.getEnd()) +
			(null != nRuleID ? " /removeRule_" + nRuleID : StringUtils.EMPTY);   
	}

	private BigDecimal getMinPrice()
	{
		BigDecimal nMinPrice = new BigDecimal(10000000);
		for(final JapanCandle oCandle : m_oHistory)
		{
			if (nMinPrice.compareTo(oCandle.getMin()) > 0)
				nMinPrice = oCandle.getMin();
		}
		
		return nMinPrice;
	}
	
	private BigDecimal getMaxPrice()
	{
		BigDecimal nMaxPrice = BigDecimal.ZERO;
		for(final JapanCandle oCandle : m_oHistory)
		{
			if (nMaxPrice.compareTo(oCandle.getMax()) < 0)
				nMaxPrice = oCandle.getMax();
		}
		
		return (m_oHistory.size() == 0 ? BigDecimal.ZERO : nMaxPrice);
	}
	
	public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
		JapanCandle oCandle = m_oHistory.get(m_oHistory.size() - 1);
		final Date oLastCandleDate = DateUtils.addMinutes(oCandle.getDate(), m_nCandleDurationMinutes); 
		if (oLastCandleDate.compareTo(new Date()) <= 0)
		{
			oCandle = new JapanCandle(); 
			m_oHistory.add(oCandle);
		}
		
		oCandle.setValue(oStateAnalysisResult.getRateAnalysisResult(m_oRateInfo).getTradesAnalysisResult().getAverageAllSumPrice());
	}
	
	public void onOccurred(final BigDecimal nPrice, final Integer nRuleID)
	{
		final String strMessage = getInfo(null) + " is occurred. " + 
			" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo) + 
			" " + BaseCommand.getCommand(GetRulesCommand.NAME);
		final ICommand oSendMessageCommand = new SendMessageCommand(strMessage);
		WorkerFactory.getMainWorker().addCommand(oSendMessageCommand);
	}
}


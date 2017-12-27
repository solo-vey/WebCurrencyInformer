package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.TaskBase;

public class TradeControler extends TaskBase
{
	private static final long serialVersionUID = 2548242166461334806L;
	
	final static public String TRADE_VOLUME = "#volume#";
	
	protected BigDecimal m_oTradeVolume; 
	protected List<ITradeTask> m_oControlTasks = new LinkedList<ITradeTask>();

	public TradeControler(RateInfo oRateInfo, String strCommandLine)
	{
		super(oRateInfo, strCommandLine, TRADE_VOLUME);
		start();
	}
	
	protected void start()
	{
		m_oTradeVolume = getParameterAsBigDecimal(TRADE_VOLUME);
	}
	
	@Override public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
	}
}

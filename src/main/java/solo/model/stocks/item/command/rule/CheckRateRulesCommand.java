package solo.model.stocks.item.command.rule;

import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.RateState;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.ISystemCommand;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class CheckRateRulesCommand extends BaseCommand implements ISystemCommand
{
	final static public String NAME = "checkRateRules";
	
	protected RateInfo m_oRateInfo;

	public CheckRateRulesCommand()
	{
		super(NAME, StringUtils.EMPTY);
	}

	public CheckRateRulesCommand(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
	}
	
	public CheckRateRulesCommand(final RateInfo oRateInfo)
	{
		super(NAME, StringUtils.EMPTY);
		m_oRateInfo = oRateInfo;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final IStockExchange oStockExchange = WorkerFactory.getStockExchange();
		
		final RateState oRateState = oStockExchange.getStockSource().getRateState(m_oRateInfo);
		oStockExchange.getLastAnalysisResult().analyse(oRateState, oStockExchange, m_oRateInfo);
		
		final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateState.getRateInfo());
		final RateState oReverseRateState = makeReverseRateState(oRateState);
		oStockExchange.getLastAnalysisResult().analyse(oReverseRateState, oStockExchange, oReverseRateInfo);
		
		final StateAnalysisResult oStateAnalysisResult = oStockExchange.getLastAnalysisResult();
		final List<Entry<Integer, IRule>> oRules = oStockExchange.getRules().getRules(m_oRateInfo);
		for(final Entry<Integer, IRule> oRuleInfo : oRules)
			oRuleInfo.getValue().check(oStateAnalysisResult);
	}
	
	public static RateState makeReverseRateState(final RateState oRateState)
	{
		final RateState oReverseRateState = new RateState(RateInfo.getReverseRate(oRateState.getRateInfo()));
		for(final Order oOrder : oRateState.getBidsOrders())
			oReverseRateState.getAsksOrders().add(TradeUtils.makeReveseOrder(oOrder));
		for(final Order oOrder : oRateState.getAsksOrders())
			oReverseRateState.getBidsOrders().add(TradeUtils.makeReveseOrder(oOrder));
		for(final Order oOrder : oRateState.getTrades())
			oReverseRateState.getTrades().add(TradeUtils.makeReveseOrder(oOrder));
		return oReverseRateState;
	}
}
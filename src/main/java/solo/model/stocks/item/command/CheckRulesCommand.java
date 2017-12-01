package solo.model.stocks.item.command;

import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.item.IRule;

/** Формат комманды 
 */
public class CheckRulesCommand extends BaseCommand
{
	final static public String NAME = "checkRules";

	final protected IStockExchange m_oStockExchange;
	
	public CheckRulesCommand(final String strStockName)
	{
		this(StringUtils.isBlank(strStockName) ? StockExchangeFactory.getDefault() : StockExchangeFactory.getStockExchange(strStockName));
	}
	
	public CheckRulesCommand(final IStockExchange oStockExchange)
	{
		super(oStockExchange.getStockName());
		m_oStockExchange = oStockExchange;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final StateAnalysisResult oStateAnalysisResult = m_oStockExchange.getHistory().getLastAnalysisResult();
		for(final Entry<Integer, IRule> oRuleInfo : m_oStockExchange.getRules().getRules().entrySet())
			oRuleInfo.getValue().check(oStateAnalysisResult, oRuleInfo.getKey());
	}
}

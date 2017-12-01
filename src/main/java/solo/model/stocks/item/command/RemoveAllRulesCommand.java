package solo.model.stocks.item.command;

import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.item.IRule;
import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;

/** Формат комманды */
public class RemoveAllRulesCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "deleteAllRules";
	
	final protected IStockExchange m_oStockExchange;
	
	public RemoveAllRulesCommand(final String strStockName)
	{
		this(StringUtils.isBlank(strStockName) ? StockExchangeFactory.getDefault() : StockExchangeFactory.getStockExchange(strStockName));
	}
	
	public RemoveAllRulesCommand(final IStockExchange oStockExchange)
	{
		super(oStockExchange.getStockName());
		m_oStockExchange = oStockExchange;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		for(final Entry<Integer, IRule> oRuleInfo : m_oStockExchange.getRules().getRules().entrySet())
			m_oStockExchange.getRules().removeRule(oRuleInfo.getKey());
	
		final ICommand oCommand = new SendMessageCommand(TransportFactory.getTransport(TelegramTransport.NAME), "All rules deleted");
		WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oCommand);
	}
}

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

/** Формат комманды 
 */
public class GetRulesCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "getRules";
	
	final protected IStockExchange m_oStockExchange;
	
	public GetRulesCommand(final String strStockName)
	{
		this(StringUtils.isBlank(strStockName) ? StockExchangeFactory.getDefault() : StockExchangeFactory.getStockExchange(strStockName));
	}
	
	public GetRulesCommand(final IStockExchange oStockExchange)
	{
		super(oStockExchange.getStockName());
		m_oStockExchange = oStockExchange;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		String strMessage = StringUtils.EMPTY;
		for(final Entry<Integer, IRule> oRule : m_oStockExchange.getRules().getRules().entrySet())
			strMessage += oRule.getValue().getInfo(oRule.getKey()) + "\r\n";

		if (StringUtils.isNotBlank(strMessage))
			strMessage += " " + BaseCommand.getCommand(RemoveAllRulesCommand.NAME);
		else 
			strMessage += "No rules";
			
		final ICommand oCommand = new SendMessageCommand(TransportFactory.getTransport(TelegramTransport.NAME), strMessage);
		WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oCommand);
	}
}

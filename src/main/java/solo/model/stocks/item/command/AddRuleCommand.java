package solo.model.stocks.item.command;

import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RulesFactory;
import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;

/** Формат комманды 
 */
public class AddRuleCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "addRule";
	
	final protected String m_strRuleInfo;
	
	public AddRuleCommand(final String strRuleInfo)
	{
		super(strRuleInfo);
		m_strRuleInfo = strRuleInfo;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final IRule oRule = RulesFactory.getRule(m_strRuleInfo);
		StockExchangeFactory.getDefault().getRules().addRule(oRule);
		
		final String strMessage = "Rule " + getInfo() + " add. " + BaseCommand.getCommand(GetRulesCommand.NAME);
		final ICommand oCommand = new SendMessageCommand(TransportFactory.getTransport(TelegramTransport.NAME), strMessage);
		WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oCommand);
	}
}

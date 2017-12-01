package solo.model.stocks.item.command;

import solo.model.stocks.exchange.IStockExchange;
import solo.model.stocks.exchange.StockExchangeFactory;
import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;

/** Формат комманды */
public class RemoveRuleCommand extends BaseCommand
{
	final static public String NAME = "removeRule";
	final static public String TEMPLATE = NAME + "_%s";

	final protected IStockExchange m_oStockExchange;
	final protected Integer m_nRuleID;
	final protected boolean m_bIsSilent;
	
	public RemoveRuleCommand(final String strRuleID)
	{
		this(strRuleID, false);
	}
	
	public RemoveRuleCommand(final String strRuleID, final boolean bIsSilent)
	{
		this(StockExchangeFactory.getDefault(), Integer.valueOf(strRuleID), bIsSilent);
	}
	
	public RemoveRuleCommand(final IStockExchange oStockExchange, final int nRuleID, final boolean bIsSilent)
	{
		super(oStockExchange.getStockName() + " " + nRuleID);
		m_oStockExchange = oStockExchange;
		m_nRuleID = nRuleID;
		m_bIsSilent = bIsSilent;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		m_oStockExchange.getRules().removeRule(m_nRuleID);
	
		if (!m_bIsSilent)
		{
			final ICommand oCommand = new SendMessageCommand(TransportFactory.getTransport(TelegramTransport.NAME), "Rule " + m_nRuleID + " deleted. " +
					BaseCommand.getCommand(GetRulesCommand.NAME));
			WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oCommand);
		}
	}
}


package solo.model.stocks.item.command.rule;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды */
public class RemoveRuleCommand extends BaseCommand
{
	final static public String NAME = "removeRule";
	final static public String ID_PARAMETER = "#id#";

	final protected Integer m_nRuleID;
	final protected boolean m_bIsSilent;
	
	public RemoveRuleCommand(final String strRuleID)
	{
		this(strRuleID, false);
	}
	
	public RemoveRuleCommand(final Integer nRuleID, final boolean bIsSilent)
	{
		this(nRuleID.toString());
	}

	public RemoveRuleCommand(final String strRuleID, final boolean bIsSilent)
	{
		super(strRuleID, ID_PARAMETER);
		m_nRuleID = getParameterAsInt(ID_PARAMETER);
		m_bIsSilent = bIsSilent;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		if (m_nRuleID.equals(Integer.MIN_VALUE))
			WorkerFactory.getMainWorker().sendMessage("Bad rule identifier [" + m_nRuleID + "]" + BaseCommand.getCommand(GetRulesCommand.NAME));
		else
		{
			WorkerFactory.getStockExchange().getRules().removeRule(m_nRuleID);
			if (!m_bIsSilent)
				WorkerFactory.getMainWorker().sendMessage("Rule " + m_nRuleID + " deleted. " + BaseCommand.getCommand(GetRulesCommand.NAME));
		}
	}
}


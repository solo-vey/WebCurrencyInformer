package solo.model.stocks.item.command.rule;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды */
public class RemoveRuleCommand extends BaseCommand
{
	public static final String NAME = "removeRule";
	public static final String ID_PARAMETER = "#id#";

	protected final Integer m_nRuleID;
	protected final boolean m_bIsSilent;
	
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
	
	@Override public void execute() throws Exception
	{
		super.execute();
		
		if (m_nRuleID.equals(Integer.MIN_VALUE))
			WorkerFactory.getMainWorker().sendSystemMessage("Bad rule identifier [" + m_nRuleID + "]" + BaseCommand.getCommand(GetRulesCommand.NAME));
		else
		{
			WorkerFactory.getStockExchange().getRules().removeRule(m_nRuleID);
			if (!m_bIsSilent)
				WorkerFactory.getMainWorker().sendSystemMessage("Rule " + m_nRuleID + " deleted");
		}
	}
}


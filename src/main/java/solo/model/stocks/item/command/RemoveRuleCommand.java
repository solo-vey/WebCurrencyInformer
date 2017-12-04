package solo.model.stocks.item.command;

/** Формат комманды */
public class RemoveRuleCommand extends BaseCommand
{
	final static public String NAME = "removeRule";
	final static public String TEMPLATE = NAME + "_%s";

	final protected Integer m_nRuleID;
	final protected boolean m_bIsSilent;
	
	public RemoveRuleCommand(final String strRuleID)
	{
		this(strRuleID, false);
	}
	
	public RemoveRuleCommand(final String strRuleID, final boolean bIsSilent)
	{
		this(Integer.valueOf(strRuleID), bIsSilent);
	}
	
	public RemoveRuleCommand(final Integer nRuleID, final boolean bIsSilent)
	{
		super(nRuleID.toString());
		m_nRuleID = nRuleID;
		m_bIsSilent = bIsSilent;
	}
	
	public void execute() throws Exception
	{
		super.execute();
		getStockExchange().getRules().removeRule(m_nRuleID);
	
		if (!m_bIsSilent)
		{
			final ICommand oCommand = new SendMessageCommand("Rule " + m_nRuleID + " deleted. " + BaseCommand.getCommand(GetRulesCommand.NAME));
			getMainWorker().addCommand(oCommand);
		}
	}
}


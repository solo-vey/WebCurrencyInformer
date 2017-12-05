package solo.model.stocks.item.command;

/** Формат комманды 
 */
public class RemoveOrderCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "removeOrder";
	final static public String TEMPLATE = NAME + "_%s";
	
	final protected String m_strOrderId;
	
	public RemoveOrderCommand(final String strOrderId)
	{
		super(strOrderId);
		m_strOrderId = strOrderId;
	}
	
	public void execute() throws Exception
	{
		super.execute();

		getStockExchange().getStockSource().removeOrder(m_strOrderId);
		
		final String strMessage = "Order " + m_strOrderId + " deleted. " + BaseCommand.getCommand(GetStockInfoCommand.NAME);
		final ICommand oCommand = new SendMessageCommand(strMessage);
		getMainWorker().addCommand(oCommand);
	}
}

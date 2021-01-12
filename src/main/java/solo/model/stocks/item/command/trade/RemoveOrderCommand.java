package solo.model.stocks.item.command.trade;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class RemoveOrderCommand extends BaseCommand
{
	public static final String NAME = "removeOrder";
	public static final String ID_PARAMETER = "#id#";
	
	protected final String m_strOrderId;
	
	public RemoveOrderCommand(final String strOrderId)
	{
		super(strOrderId, ID_PARAMETER);
		m_strOrderId = getParameter(ID_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		WorkerFactory.getStockExchange().getStockSource().removeOrder(m_strOrderId, null);
		
		WorkerFactory.getMainWorker().sendSystemMessage("Order " + m_strOrderId + " deleted. " + BaseCommand.getCommand(GetStockInfoCommand.NAME));
	}
}

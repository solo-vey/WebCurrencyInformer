package solo.model.stocks.item.command.trade;

import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.model.stocks.worker.WorkerFactory;

/** Формат комманды 
 */
public class RemoveOrderCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "removeOrder";
	final static public String ID_PARAMETER = "#id#";
	
	final protected String m_strOrderId;
	
	public RemoveOrderCommand(final String strOrderId)
	{
		super(strOrderId, ID_PARAMETER);
		m_strOrderId = getParameter(ID_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		WorkerFactory.getStockExchange().getStockSource().removeOrder(m_strOrderId);
		
		WorkerFactory.getMainWorker().sendMessage("Order " + m_strOrderId + " deleted. " + BaseCommand.getCommand(GetStockInfoCommand.NAME));
	}
}

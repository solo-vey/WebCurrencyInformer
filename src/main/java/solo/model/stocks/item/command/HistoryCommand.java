package solo.model.stocks.item.command;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;

/** Формат комманды 
 */
public class HistoryCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "history";

	public HistoryCommand(final String strCommandLine)
	{
		super(strCommandLine);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		String strMessage = StringUtils.EMPTY;
		for(final String strCommand : BaseCommand.getHistory())
			strMessage += "/"  + strCommand + "\r\n";
		strMessage = (StringUtils.isNotBlank(strMessage) ? strMessage : "History is empty");

		final ICommand oCommand = new SendMessageCommand(TransportFactory.getTransport(TelegramTransport.NAME), strMessage);
		WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oCommand);
	}
}

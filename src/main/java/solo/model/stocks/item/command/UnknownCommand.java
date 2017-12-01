package solo.model.stocks.item.command;

import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;

/** Формат комманды 
 */
public class UnknownCommand extends BaseCommand
{
	public UnknownCommand(final String strCommandLine)
	{
		super(strCommandLine);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final ICommand oCommand = new SendMessageCommand(TransportFactory.getTransport(TelegramTransport.NAME), getInfo());
		WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oCommand);
	}
}

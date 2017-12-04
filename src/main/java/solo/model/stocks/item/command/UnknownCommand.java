package solo.model.stocks.item.command;

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
		final ICommand oCommand = new SendMessageCommand(getInfo());
		getMainWorker().addCommand(oCommand);
	}
}

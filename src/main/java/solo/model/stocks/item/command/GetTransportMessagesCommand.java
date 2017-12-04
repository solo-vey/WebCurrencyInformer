package solo.model.stocks.item.command;

import solo.transport.ITransportMessage;
import solo.transport.ITransportMessages;

/** Формат комманды 
 */
public class GetTransportMessagesCommand extends BaseCommand
{
	final static public String NAME = "getMessages";

	public GetTransportMessagesCommand()
	{
		super();
	}

	public GetTransportMessagesCommand(final String strCommandLine)
	{
		super(strCommandLine);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		final ITransportMessages oMessages = getTransport().getMessages();
		if (null == oMessages)
			return;
		
		for(final ITransportMessage oMessage : oMessages.getMessages())
		{
			final String strCommandLine = oMessage.getText();
			final ICommand oCommand = CommandFactory.getCommand(strCommandLine);
			getMainWorker().addCommand(oCommand);
		}
	}
}

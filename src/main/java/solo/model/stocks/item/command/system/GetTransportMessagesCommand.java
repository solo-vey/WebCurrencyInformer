package solo.model.stocks.item.command.system;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.ICommand;
import solo.transport.ITransportMessage;
import solo.transport.ITransportMessages;

/** Формат комманды 
 */
public class GetTransportMessagesCommand extends BaseCommand implements ISystemCommand
{
	final static public String NAME = "getMessages";

	public GetTransportMessagesCommand()
	{
		super(StringUtils.EMPTY, StringUtils.EMPTY);
	}

	public GetTransportMessagesCommand(final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
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

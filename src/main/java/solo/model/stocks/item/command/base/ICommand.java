package solo.model.stocks.item.command.base;

public interface ICommand
{
	void execute() throws Exception;
	String getInfo();
	String getCommandLine();
	String getHelp();
}

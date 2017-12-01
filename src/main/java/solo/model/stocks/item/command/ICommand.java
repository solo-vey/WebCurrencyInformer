package solo.model.stocks.item.command;

public interface ICommand
{
	void execute() throws Exception;
	String getInfo();
}

package solo.model.stocks.worker;

import solo.model.stocks.item.command.ICommand;

public interface IWorker extends Runnable
{
	void addCommand(final ICommand oCommand);
	void startWorker();
	void stopWorker();
}

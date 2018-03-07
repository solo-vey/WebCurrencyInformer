package solo.model.stocks.worker;

import solo.model.stocks.item.command.base.ICommand;

public interface IMainWorker
{
	StockWorker getStockWorker();
	void addCommand(final ICommand oCommand);
}

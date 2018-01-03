package solo.model.stocks.item.rules.task.trade;

import solo.model.stocks.item.IRule;

public interface ITradeTask extends IRule
{
	TradeInfo getTradeInfo();
	ITradeControler getTradeControler();
	void setTradeControler(final ITradeControler oTradeControler);
}

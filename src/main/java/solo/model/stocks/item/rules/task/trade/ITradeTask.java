package solo.model.stocks.item.rules.task.trade;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.Order;

public interface ITradeTask extends IRule
{
	TradeInfo getTradeInfo();
	public void updateOrderTradeInfo(final Order oGetOrder);
	ITradeControler getTradeControler();
	void setTradeControler(final ITradeControler oTradeControler);
}

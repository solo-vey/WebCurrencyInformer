package solo.model.stocks.item.rules.task.trade;

import java.math.BigDecimal;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.worker.WorkerFactory;

public class SellTaskTrade extends TaskTrade
{
	public static final String NAME = "SELLTRADE";

	private static final long serialVersionUID = -178333243757975169L;

	public SellTaskTrade(final String strCommandLine) throws Exception
	{
		super(strCommandLine);
		starTask();
	}

	public SellTaskTrade(final String strCommandLine, final String strTemplate) throws Exception
	{
		super(strCommandLine, strTemplate);
		starTask();
	}

	@Override public String getType()
	{
		return NAME;   
	}
	
	public void starTask() throws Exception
	{
		super.starTask();
		
		getTradeInfo().addBuy(BigDecimal.ZERO, getParameterAsBigDecimal(TRADE_VOLUME));
		getTradeInfo().setCriticalPrice(BigDecimal.ZERO);
		getTradeInfo().setOrder(Order.NULL);
		getTradeInfo().setTaskSide(OrderSide.SELL);
		WorkerFactory.getStockExchange().getRules().save();
	}
}


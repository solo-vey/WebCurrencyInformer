package solo.model.stocks.item.rules.task.trade;

import solo.model.stocks.worker.WorkerFactory;

public class BuyTradeControler extends TradeControler
{
	private static final long serialVersionUID = -2333740994826597728L;
	
	public static final String NAME = "BUYCONTROLER";

	public BuyTradeControler(String strCommandLine)
	{
		super(strCommandLine);
	}

	@Override protected String getTraderName()
	{
		return BuyTaskTrade.NAME;
	}
	
	public void tradeDone(final TaskTrade oTaskTrade)
	{
		super.tradeDone(oTaskTrade);

		WorkerFactory.getStockExchange().getRules().removeRule(this);
	}	
}

package solo.transport.telegram;

import solo.model.stocks.exchange.Stocks;

public class PoloniexTelegramTransport extends TelegramTransport
{
	public PoloniexTelegramTransport()
	{
		super(Stocks.Poloniex.toString());
	}
	
	public PoloniexTelegramTransport(String strBotName)
	{
		super(strBotName);
	}
}

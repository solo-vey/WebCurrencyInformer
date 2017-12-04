package solo.transport.telegram;

import solo.model.stocks.exchange.Stocks;

public class KunaTelegramTransport extends TelegramTransport
{
	public KunaTelegramTransport()
	{
		super(Stocks.Kuna.toString());
	}
	
	public KunaTelegramTransport(String strBotName)
	{
		super(strBotName);
	}
}

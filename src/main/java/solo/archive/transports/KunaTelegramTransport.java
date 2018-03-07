package solo.archive.transports;

import solo.model.stocks.exchange.Stocks;
import solo.transport.telegram.TelegramTransport;

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

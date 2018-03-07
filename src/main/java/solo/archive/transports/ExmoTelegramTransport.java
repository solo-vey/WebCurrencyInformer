package solo.archive.transports;

import solo.model.stocks.exchange.Stocks;
import solo.transport.telegram.TelegramTransport;

public class ExmoTelegramTransport extends TelegramTransport
{
	public ExmoTelegramTransport()
	{
		super(Stocks.Exmo.toString());
	}
	
	public ExmoTelegramTransport(String strBotName)
	{
		super(strBotName);
	}
}

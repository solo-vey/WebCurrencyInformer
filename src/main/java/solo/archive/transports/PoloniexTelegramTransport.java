package solo.archive.transports;

import solo.model.stocks.exchange.Stocks;
import solo.transport.telegram.TelegramTransport;

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

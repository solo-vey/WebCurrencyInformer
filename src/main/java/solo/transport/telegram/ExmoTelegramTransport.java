package solo.transport.telegram;

import solo.model.stocks.exchange.Stocks;

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

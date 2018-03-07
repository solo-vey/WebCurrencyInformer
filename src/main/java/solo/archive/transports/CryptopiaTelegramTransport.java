package solo.archive.transports;

import solo.model.stocks.exchange.Stocks;
import solo.transport.telegram.TelegramTransport;

public class CryptopiaTelegramTransport extends TelegramTransport
{
	public CryptopiaTelegramTransport()
	{
		super(Stocks.Cryptopia.toString());
	}
	
	public CryptopiaTelegramTransport(String strBotName)
	{
		super(strBotName);
	}
}

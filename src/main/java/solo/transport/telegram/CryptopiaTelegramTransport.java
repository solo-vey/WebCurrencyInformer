package solo.transport.telegram;

import solo.model.stocks.exchange.Stocks;

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

package solo.archive.transports;

import solo.model.stocks.exchange.Stocks;
import solo.transport.telegram.TelegramTransport;

public class BtcTradeTelegramTransport extends TelegramTransport
{
	public BtcTradeTelegramTransport()
	{
		super(Stocks.BtcTrade.toString());
	}
	
	public BtcTradeTelegramTransport(String strBotName)
	{
		super(strBotName);
	}
}

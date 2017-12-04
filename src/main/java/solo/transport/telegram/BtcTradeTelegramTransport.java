package solo.transport.telegram;

import solo.model.stocks.exchange.Stocks;

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

package solo.model.stocks.item.command;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockUserInfo;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class GetStockInfoCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "getStockInfo";
	
	public GetStockInfoCommand(final String strСommandLine)
	{
		super(strСommandLine);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final StockUserInfo oUserInfo = getStockExchange().getStockSource().getUserInfo();
		
		String strMessage = StringUtils.EMPTY;
		for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oUserInfo.getMoney().entrySet())
		{
			strMessage += oCurrencyInfo.getKey() + "/" + MathUtils.toCurrencyStringEx(oCurrencyInfo.getValue().getBalance()) + 
							"/" + MathUtils.toCurrencyStringEx(oCurrencyInfo.getValue().getLocked()) + "\r\n";
		}

		for(final Entry<RateInfo, List<Order>> oOrdersInfo : oUserInfo.getOrders().entrySet())
		{
			for(final Order oOrder : oOrdersInfo.getValue())
			{
				strMessage += oOrder.getState() + "/" + oOrdersInfo.getKey().getCurrencyFrom() + "/" + MathUtils.toCurrencyString(oOrder.getPrice()) + 
							"/" + MathUtils.toCurrencyStringEx(oOrder.getVolume()) + "/" + MathUtils.toCurrencyString(oOrder.getSum()) +
							" " + BaseCommand.getCommand(RemoveOrderCommand.TEMPLATE, oOrder.getId()) + "\r\n";
			}
		}

		ICommand oCommand = new SendMessageCommand(strMessage);
		getMainWorker().addCommand(oCommand);
	}
}

package solo.model.stocks.item.command.trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import solo.model.currency.Currency;
import solo.model.currency.CurrencyAmount;
import solo.model.stocks.item.Order;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.StockUserInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class GetStockInfoCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "getStockInfo";
	final static public String RATE_PARAMETER = "#rate#";
	
	final protected RateInfo m_oRateInfo;  
	
	public GetStockInfoCommand(final String strСommandLine)
	{
		super(strСommandLine, RATE_PARAMETER);
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		
		final StockUserInfo oUserInfo = getStockExchange().getStockSource().getUserInfo(m_oRateInfo);
		
		String strMessage = StringUtils.EMPTY;
		for(final Entry<Currency, CurrencyAmount> oCurrencyInfo : oUserInfo.getMoney().entrySet())
		{
			strMessage += oCurrencyInfo.getKey() + "/" + MathUtils.toCurrencyStringEx(oCurrencyInfo.getValue().getBalance()) + 
							(oCurrencyInfo.getValue().getLocked().compareTo(BigDecimal.ZERO) != 0 ? "/" + MathUtils.toCurrencyStringEx(oCurrencyInfo.getValue().getLocked()) : StringUtils.EMPTY)
							+ "\r\n";
		}

		for(final Entry<RateInfo, List<Order>> oOrdersInfo : oUserInfo.getOrders().entrySet())
		{
			for(final Order oOrder : oOrdersInfo.getValue())
			{
				strMessage += oOrder.getState() + "/" + oOrdersInfo.getKey().getCurrencyFrom() + "/" + MathUtils.toCurrencyString(oOrder.getPrice()) + 
							"/" + MathUtils.toCurrencyStringEx(oOrder.getVolume()) + "/" + MathUtils.toCurrencyString(oOrder.getSum()) +
							" " + CommandFactory.makeCommandLine(RemoveOrderCommand.class, RemoveOrderCommand.ID_PARAMETER, oOrder.getId()) + "\r\n";
			}
		}

		sendMessage(strMessage);
	}
}

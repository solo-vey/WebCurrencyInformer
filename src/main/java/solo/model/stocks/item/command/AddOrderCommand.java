package solo.model.stocks.item.command;

import java.math.BigDecimal;

import solo.model.currency.Currency;
import solo.model.stocks.item.RateInfo;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

/** Формат комманды 
 */
public class AddOrderCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "addOrder";
	
	final protected String m_strSide;
	final protected RateInfo m_oRateInfo; 
	final protected BigDecimal m_nPrice; 
	final protected BigDecimal m_nVolume; 
	
	public AddOrderCommand(final String strOrderInfo)
	{
		super(strOrderInfo);
		m_strSide = CommonUtils.splitFirst(strOrderInfo).toLowerCase();
		final Currency oCurrency = Currency.valueOf(CommonUtils.splitToPos(strOrderInfo, 1).toUpperCase());
		m_oRateInfo = new RateInfo(oCurrency, Currency.UAH);
		m_nPrice = MathUtils.fromString(CommonUtils.splitToPos(strOrderInfo, 2).toUpperCase());
		m_nVolume = MathUtils.fromString(CommonUtils.splitToPos(strOrderInfo, 3).toUpperCase());
	}
	
	public void execute() throws Exception
	{
		super.execute();
		getStockExchange().getStockSource().addOrder(m_strSide, m_oRateInfo, m_nVolume, m_nPrice);
		
		final String strMessage = "Order " + m_strSide + "/" + m_oRateInfo + "/" + m_nPrice + "/" + m_nVolume + " add. " + BaseCommand.getCommand(GetStockInfoCommand.NAME);
		final ICommand oCommand = new SendMessageCommand(strMessage);
		getMainWorker().addCommand(oCommand);
	}
}

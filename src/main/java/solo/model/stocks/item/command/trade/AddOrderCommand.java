package solo.model.stocks.item.command.trade;

import java.math.BigDecimal;

import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.system.IHistoryCommand;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class AddOrderCommand extends BaseCommand implements IHistoryCommand
{
	final static public String NAME = "addOrder";
	final static public String SIDE_PARAMETER = "#side#";
	final static public String RATE_PARAMETER = "#rate#";
	final static public String PRICE_PARAMETER = "#price#";
	final static public String VOLUME_PARAMETER = "#volume#";
	
	final protected String m_strSide;
	final protected RateInfo m_oRateInfo; 
	final protected BigDecimal m_nPrice; 
	final protected BigDecimal m_nVolume; 
	
	public AddOrderCommand(final String strOrderInfo)
	{
		super(strOrderInfo, CommonUtils.mergeParameters(SIDE_PARAMETER, RATE_PARAMETER, PRICE_PARAMETER, VOLUME_PARAMETER));
		m_strSide = getParameter(SIDE_PARAMETER).toLowerCase();
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
		m_nPrice = getParameterAsBigDecimal(PRICE_PARAMETER);
		m_nVolume = getParameterAsBigDecimal(VOLUME_PARAMETER);
	}
	
	public void execute() throws Exception
	{
		super.execute();
		getStockExchange().getStockSource().addOrder(m_strSide, m_oRateInfo, m_nVolume, m_nPrice);
		
		sendMessage("Order " + m_strSide + "/" + m_oRateInfo + "/" + m_nPrice + "/" + m_nVolume + " add. " + BaseCommand.getCommand(GetStockInfoCommand.NAME));
	}
}

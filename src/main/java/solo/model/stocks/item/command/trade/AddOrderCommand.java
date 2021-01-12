package solo.model.stocks.item.command.trade;

import java.math.BigDecimal;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.CommonUtils;

/** Формат комманды 
 */
public class AddOrderCommand extends BaseCommand
{
	public static final String NAME = "addOrder";
	public static final String SIDE_PARAMETER = "#side#";
	public static final String RATE_PARAMETER = "#rate#";
	public static final String PRICE_PARAMETER = "#price#";
	public static final String VOLUME_PARAMETER = "#volume#";
	
	protected final OrderSide m_oSide;
	protected final RateInfo m_oRateInfo; 
	protected final BigDecimal m_nPrice; 
	protected final BigDecimal m_nVolume; 
	
	public AddOrderCommand(final String strOrderInfo)
	{
		super(strOrderInfo, CommonUtils.mergeParameters(SIDE_PARAMETER, RATE_PARAMETER, PRICE_PARAMETER, VOLUME_PARAMETER));
		m_oSide = (OrderSide) getParameterAsEnum(SIDE_PARAMETER, OrderSide.class);
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
		m_nPrice = getParameterAsBigDecimal(PRICE_PARAMETER);
		m_nVolume = getParameterAsBigDecimal(VOLUME_PARAMETER);
	}
	
	@Override public void execute() throws Exception
	{
		super.execute();
		final Order oOrder = WorkerFactory.getStockSource().addOrder(m_oSide, m_oRateInfo, m_nVolume, m_nPrice);
		
		if (oOrder.isNull())
			WorkerFactory.getMainWorker().sendSystemMessage("Can't add order. " + oOrder.getInfo() + " " + BaseCommand.getCommand(GetStockInfoCommand.NAME));
		else
			WorkerFactory.getMainWorker().sendSystemMessage("Order " + oOrder.getInfo() + " add. " + BaseCommand.getCommand(GetStockInfoCommand.NAME));
	}
}

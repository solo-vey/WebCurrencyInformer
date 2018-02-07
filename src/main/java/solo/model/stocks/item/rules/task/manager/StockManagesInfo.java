package solo.model.stocks.item.rules.task.manager;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.BaseObject;
import solo.model.stocks.item.rules.task.trade.TaskTrade;

public class StockManagesInfo extends BaseObject implements Serializable
{
	private static final long serialVersionUID = -7601846839784506296L;

	public StockManagesInfo()
	{
	}
	
	
	public void tradeStart(final TaskTrade oTaskTrade) 
	{
		
	}
	
	public void tradeDone(final TaskTrade oTaskTrade) 
	{
		
	}
	
	public void buyDone(final TaskTrade oTaskTrade) 
	{
		
	}
	
	public void addBuy(final BigDecimal nSpendSum, final BigDecimal nBuyVolume) 
	{
		
	}
	
	public void addSell(final BigDecimal nReceiveSum, final BigDecimal nSoldVolume) 
	{
		
	} 
	
	/** Строковое представление документа */
	@Override public String toString()
	{
		String strResult = StringUtils.EMPTY;

		return strResult;
	}
}

package solo.model.stocks.item.rules.task.manager;

import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.rules.task.trade.ITest;
import solo.model.stocks.worker.WorkerFactory;

public class ManagerUtils
{
	public static boolean isTestObject(final Object oObject)
	{
		return oObject instanceof ITest;
	}
	
	public static boolean isHasRealRules(final RateInfo oRateInfo)
	{
		boolean bIsHasRealRule = false;
		for(final IRule oRule : WorkerFactory.getStockExchange().getRules().getRules().values())
		{
			if (!oRule.getRateInfo().equals(oRateInfo))
				continue;
			
			bIsHasRealRule |= !isTestObject(oRule); 
		}
		
		return bIsHasRealRule;
	}
	
	public static boolean isHasTestRules(final RateInfo oRateInfo)
	{
		boolean bIsHasTestRule = false;
		for(final IRule oRule : WorkerFactory.getStockExchange().getRules().getRules().values())
		{
			if (!oRule.getRateInfo().equals(oRateInfo))
				continue;
			
			bIsHasTestRule |= isTestObject(oRule); 
		}
		
		return bIsHasTestRule;
	}
}


package solo.model.stocks.item.rules.task.strategy;

import java.util.HashMap;
import java.util.Map;

public class StrategyFactory
{
	protected static Map<String, IBuyStrategy> s_oBuyStrategies = new HashMap<String, IBuyStrategy>();
	protected static Map<String, ISellStrategy> s_oSellStrategies = new HashMap<String, ISellStrategy>();
	
	static
	{
		addBuyStrategy(new QuickBuyStrategy());
		addBuyStrategy(new QuickBuyExStrategy());
		addBuyStrategy(new CarefullBuyStrategy());
		addBuyStrategy(new NowBuyStrategy());
		
		addSellStrategy(new QuickSellStrategy());
		addSellStrategy(new NowSellStrategy());
	}
	
	static public void addBuyStrategy(final IBuyStrategy oBuyStrategy)
	{
		s_oBuyStrategies.put(oBuyStrategy.getName().toLowerCase(), oBuyStrategy);
	}
	
	static public void addSellStrategy(final ISellStrategy oSellStrategy)
	{
		s_oSellStrategies.put(oSellStrategy.getName().toLowerCase(), oSellStrategy);
	}

	static public IBuyStrategy getBuyStrategy(final String strStrategyName)
	{
		return s_oBuyStrategies.get(strStrategyName.toLowerCase());
	}

	static public ISellStrategy getSellStrategy(final String strStrategyName)
	{
		return s_oSellStrategies.get(strStrategyName.toLowerCase());
	}
}

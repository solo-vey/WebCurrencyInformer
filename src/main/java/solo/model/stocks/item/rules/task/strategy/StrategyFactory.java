package solo.model.stocks.item.rules.task.strategy;

import java.util.HashMap;
import java.util.Map;

import solo.model.stocks.item.rules.task.strategy.trade.DropSellTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.ITradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.ReverseTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.SimpleTradeStrategy;

public class StrategyFactory
{
	protected static Map<String, IBuyStrategy> s_oBuyStrategies = new HashMap<String, IBuyStrategy>();
	protected static Map<String, ISellStrategy> s_oSellStrategies = new HashMap<String, ISellStrategy>();
	protected static Map<String, ITradeStrategy> s_oTradeStrategies = new HashMap<String, ITradeStrategy>();
	
	static
	{
		addBuyStrategy(new QuickBuyStrategy());
		addBuyStrategy(new QuickBuyExStrategy());
		addBuyStrategy(new CarefullBuyStrategy());
		addBuyStrategy(new NowBuyStrategy());
		
		addSellStrategy(new QuickSellStrategy());
		addSellStrategy(new NowSellStrategy());
		
		addTradeStrategy(new SimpleTradeStrategy());
		addTradeStrategy(new DropSellTradeStrategy());
		addTradeStrategy(new ReverseTradeStrategy());
	}
	
	static public void addBuyStrategy(final IBuyStrategy oBuyStrategy)
	{
		s_oBuyStrategies.put(oBuyStrategy.getName().toLowerCase(), oBuyStrategy);
	}
	
	static public void addSellStrategy(final ISellStrategy oSellStrategy)
	{
		s_oSellStrategies.put(oSellStrategy.getName().toLowerCase(), oSellStrategy);
	}
	
	static public void addTradeStrategy(final ITradeStrategy oTradeStrategy)
	{
		s_oTradeStrategies.put(oTradeStrategy.getName().toLowerCase(), oTradeStrategy);
	}

	static public IBuyStrategy getBuyStrategy(final String strStrategyName)
	{
		return s_oBuyStrategies.get(strStrategyName.toLowerCase());
	}

	static public ISellStrategy getSellStrategy(final String strStrategyName)
	{
		return s_oSellStrategies.get(strStrategyName.toLowerCase());
	}
	
	static public ITradeStrategy getTradeStrategy(final String strStrategyName)
	{
		return s_oTradeStrategies.get(strStrategyName.toLowerCase());
	}
}

package solo.model.stocks.item.rules.task.strategy;

import java.util.HashMap;
import java.util.Map;

import solo.model.stocks.item.rules.task.strategy.trade.CalmRateTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.DropSellTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.ITradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.ReverseTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.SimpleTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.CarefullBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.CurrentPriceBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.IBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.ISellStrategy;
import solo.model.stocks.item.rules.task.strategy.NowBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.NowSellStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickBuyExStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.QuickSellStrategy;

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
		addBuyStrategy(new CurrentPriceBuyStrategy());
		
		addSellStrategy(new QuickSellStrategy());
		addSellStrategy(new NowSellStrategy());
		addSellStrategy(new HalfSellStrategy());
		
		addTradeStrategy(new SimpleTradeStrategy());
		addTradeStrategy(new DropSellTradeStrategy());
		addTradeStrategy(new ReverseTradeStrategy());
		addTradeStrategy(new CalmRateTradeStrategy());
	}
	
	public static void addBuyStrategy(final IBuyStrategy oBuyStrategy)
	{
		s_oBuyStrategies.put(oBuyStrategy.getName().toLowerCase(), oBuyStrategy);
	}
	
	public static void addSellStrategy(final ISellStrategy oSellStrategy)
	{
		s_oSellStrategies.put(oSellStrategy.getName().toLowerCase(), oSellStrategy);
	}
	
	public static void addTradeStrategy(final ITradeStrategy oTradeStrategy)
	{
		s_oTradeStrategies.put(oTradeStrategy.getName().toLowerCase(), oTradeStrategy);
	}

	public static IBuyStrategy getBuyStrategy(final String strStrategyName)
	{
		return s_oBuyStrategies.get(strStrategyName.toLowerCase());
	}

	public static ISellStrategy getSellStrategy(final String strStrategyName)
	{
		return s_oSellStrategies.get(strStrategyName.toLowerCase());
	}
	
	public static ITradeStrategy getTradeStrategy(final String strStrategyName)
	{
		return s_oTradeStrategies.get(strStrategyName.toLowerCase());
	}
}

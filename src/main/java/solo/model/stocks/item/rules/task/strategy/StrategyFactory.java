package solo.model.stocks.item.rules.task.strategy;

import java.util.HashMap;
import java.util.Map;

import solo.model.stocks.item.rules.task.strategy.controler.DropSellTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.controler.ITradeStrategy;
import solo.model.stocks.item.rules.task.strategy.controler.ReverseTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.controler.SimpleTradeStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.CarefullBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.IBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.ISellStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.NowBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.NowSellStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.QuickBuyExStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.QuickBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.trade.QuickSellStrategy;

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

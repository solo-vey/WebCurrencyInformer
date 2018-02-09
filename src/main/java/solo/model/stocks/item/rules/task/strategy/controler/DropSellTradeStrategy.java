package solo.model.stocks.item.rules.task.strategy.controler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import solo.model.stocks.item.Order;
import solo.model.stocks.item.OrderSide;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.analyse.Candlestick;
import solo.model.stocks.item.analyse.StockCandlestick;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.trade.GetTradeInfoCommand;
import solo.model.stocks.item.rules.task.strategy.trade.CarefullBuyStrategy;
import solo.model.stocks.item.rules.task.strategy.StrategyFactory;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeControler;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.item.rules.task.trade.TradesInfo;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.MessageLevel;
import solo.utils.MathUtils;

public class DropSellTradeStrategy extends SimpleTradeStrategy
{
	private static final long serialVersionUID = 451339411265580800L;
	
	public static final String NAME = "DropSellTrade";

	@Override public String getName()
	{
		return NAME;
	}	
	
	@Override public boolean checkTrade(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		super.checkTrade(oTaskTrade, aTaskTrades, oTradeControler);
		
		setCarefullBuyIfFall(oTaskTrade, oTradeControler);
		resetCriticalPriceForSellOrder(oTaskTrade, aTaskTrades, oTradeControler);
		
		return false;
	}

	protected void setCarefullBuyIfFall(final ITradeTask oTaskTrade, final TradeControler oTradeControler)
	{
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
		if (!OrderSide.BUY.equals(oOrder.getSide()))
			return;
		
		final RateInfo oRateInfo = oTaskTrade.getTradeInfo().getRateInfo();
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oRateInfo);
		if (!oCandlestick.isLongFall())
		{
			oTaskTrade.getTradeInfo().restoreDefaultBuyStrategy();
			return;
		}
		
		oTaskTrade.getTradeInfo().setBuyStrategy(StrategyFactory.getBuyStrategy(CarefullBuyStrategy.NAME));		
		return;
	}

	protected void resetCriticalPriceForSellOrder(final ITradeTask oTaskTrade, final List<ITradeTask> aTaskTrades, final TradeControler oTradeControler)
	{
		final Order oOrder = oTaskTrade.getTradeInfo().getOrder();
		if (!OrderSide.SELL.equals(oOrder.getSide()))
			return;

		final TradesInfo oTradesInfo = oTradeControler.getTradesInfo();
		final RateInfo oRateInfo = oTradesInfo.getRateInfo();
		final BigDecimal nMinTradeVolume = TradeUtils.getMinTradeVolume(oRateInfo);
		if (oOrder.getVolume().compareTo(nMinTradeVolume) < 0)
	    	return;
		
		final Date oHourDateCreate = DateUtils.addMinutes(new Date(), -60); 
	    if (null != oOrder.getCreated() && oOrder.getCreated().before(oHourDateCreate))
	    {
	    	//final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
			//final Candlestick oCandlestick = oStockCandlestick.get(oRateInfo);
			//final BigDecimal nNewCriticalPrice = oCandlestick.getMax(24);
			
	    	final BigDecimal nTotalBougthPrice = oTaskTrade.getTradeInfo().getAveragedBoughPrice(); 
	    	final BigDecimal nNewCriticalPrice = MathUtils.getBigDecimal(nTotalBougthPrice.doubleValue() * 0.85, TradeUtils.getPricePrecision(oRateInfo));
	    	setNewCriticalPrice(nNewCriticalPrice, oTaskTrade, "Drop after 60 minutes critical price ");
			return;
	    }
	    
		final Date oHalfHourDateCreate = DateUtils.addMinutes(new Date(), -30); 
	    if (null != oOrder.getCreated() && oOrder.getCreated().before(oHalfHourDateCreate))
	    {
	    	final BigDecimal nTotalBougthPrice = oTaskTrade.getTradeInfo().getAveragedBoughPrice(); 
	    	final BigDecimal nNewCriticalPrice = MathUtils.getBigDecimal(nTotalBougthPrice.doubleValue() * 0.96, TradeUtils.getPricePrecision(oRateInfo));
	    	setNewCriticalPrice(nNewCriticalPrice, oTaskTrade, "Drop after 30 minutes critical price ");
			return;
	    }
		
		final BigDecimal nBougthPrice = oTaskTrade.getTradeInfo().getAveragedBoughPrice(); 
	    final BigDecimal nTradeCommision = TradeUtils.getCommisionValue(nBougthPrice, BigDecimal.ZERO);

    	final Date oFithteenMinutesDateCreate = DateUtils.addMinutes(new Date(), -15); 
	    if (null != oOrder.getCreated() && oOrder.getCreated().before(oFithteenMinutesDateCreate))
	    {
	    	final BigDecimal nNewCriticalPrice = nBougthPrice.add(nTradeCommision);
	    	setNewCriticalPrice(nNewCriticalPrice, oTaskTrade, "Drop after 15 minutes critical price ");
			return;
	    }
		
    	final BigDecimal nTradeMargin = TradeUtils.getMarginValue(nBougthPrice);
	    final BigDecimal nCommisionAndMargin = nTradeMargin.add(nTradeCommision);
    	final BigDecimal nNewCriticalPriceMin = nBougthPrice.add(nCommisionAndMargin);
    	
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oRateInfo);		
		if (oCandlestick.isLongGrowth())
		{
	    	final BigDecimal nRoundedCriticalPriceMinPlus5Percent = MathUtils.getBigDecimal(nNewCriticalPriceMin.doubleValue() * 1.002, TradeUtils.DEFAULT_PRICE_PRECISION);
	    	final BigDecimal nNewCriticalPriceFull = TradeUtils.getRoundedCriticalPrice(oRateInfo, oTaskTrade.getTradeInfo().calculateCriticalPrice());
			if (nRoundedCriticalPriceMinPlus5Percent.compareTo(nNewCriticalPriceFull) > 0)
			{
				setNewCriticalPrice(nNewCriticalPriceFull, oTaskTrade, "Restore critical price ");
				return;
			}
		}

    	setNewCriticalPrice(nNewCriticalPriceMin, oTaskTrade, "Set less 15 minutes critical price ");
	}
	
	protected void setNewCriticalPrice(final BigDecimal nCriticalPrice, final ITradeTask oTaskTrade, final String strType)
	{
		final RateInfo oRateInfo = oTaskTrade.getTradeInfo().getRateInfo();
		final BigDecimal nRoundedCriticalPrice = TradeUtils.getRoundedCriticalPrice(oRateInfo, nCriticalPrice);
		if (nRoundedCriticalPrice.compareTo(oTaskTrade.getTradeInfo().getCriticalPrice()) == 0)
			return;
		
		final StockCandlestick oStockCandlestick = WorkerFactory.getStockExchange().getStockCandlestick();
		final Candlestick oCandlestick = oStockCandlestick.get(oRateInfo);
		
		final String strShortMessage = strType + MathUtils.toCurrencyStringEx2(nCriticalPrice) + ". Trand " + oCandlestick.getType() + " " + 
				oTaskTrade.getTradeInfo().getOrder().getInfoShort();
		
		final String strMessage = oRateInfo + "\r\n" + strShortMessage + 
    			" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, oRateInfo) + 
    			" " + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, oTaskTrade.getTradeInfo().getRuleID(), GetTradeInfoCommand.FULL_PARAMETER, "true");
		oTaskTrade.getTradeInfo().setCriticalPrice(nCriticalPrice, strShortMessage);
		WorkerFactory.getMainWorker().sendMessage(MessageLevel.DEBUG, strMessage); 
	}
}

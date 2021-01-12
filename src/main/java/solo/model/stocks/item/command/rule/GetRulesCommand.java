package solo.model.stocks.item.command.rule;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.RateAnalysisResult;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.trade.GetTradeInfoCommand;
import solo.model.stocks.item.rules.task.manager.IStockManager;
import solo.model.stocks.item.rules.task.manager.ManagerUtils;
import solo.model.stocks.item.rules.task.manager.RateTradesBlock;
import solo.model.stocks.item.rules.task.manager.TradesBlock;
import solo.model.stocks.item.rules.task.trade.ITradeControler;
import solo.model.stocks.item.rules.task.trade.ITradeTask;
import solo.model.stocks.item.rules.task.trade.TradeUtils;
import solo.model.stocks.worker.WorkerFactory;
import solo.transport.telegram.TelegramTransport;
import solo.utils.ResourceUtils;

/** Формат комманды 
 */
public class GetRulesCommand extends BaseCommand
{
	public static final String NAME = "rules";
	
	public GetRulesCommand(final String strCommandLine)
	{
		super(strCommandLine, "#type#");
	}
	
	@Override public void execute() throws Exception
	{
		super.execute();
		final String strType = getParameter("#type#").toLowerCase();
		
		final Map<RateInfo, List<IRule>> aRulesByRate = new HashMap<>();
		int nAllCount = 0;
		for(final Entry<Integer, IRule> oRuleInfo : WorkerFactory.getStockExchange().getRules().getRules().entrySet())
		{
			final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRuleInfo.getValue());
			if (null != oTradeTask && !oTradeTask.getTradeControler().equals(ITradeControler.NULL) &&
					WorkerFactory.getStockExchange().getRules().getRules().containsValue(oTradeTask.getTradeControler()))
				continue;
			
			nAllCount++;
			final RateInfo oRateInfo = oRuleInfo.getValue().getRateInfo();
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo); 
			if (strType.contains("rate:") && !strType.contains("rate:" + oRateInfo) && !strType.contains("rate:" + oReverseRateInfo))
				continue;
			
			final RateInfo oOriginalRateInfo = (oRateInfo.getIsReverse() ? RateInfo.getReverseRate(oRateInfo) : oRateInfo); 
			if (!aRulesByRate.containsKey(oOriginalRateInfo))
				aRulesByRate.put(oOriginalRateInfo, new LinkedList<IRule>());
			
			aRulesByRate.get(oOriginalRateInfo).add(oRuleInfo.getValue());
		}
		
		RateInfo oSelectedRateInfo = null;
		final boolean bIsShowIfHasReal = !strType.contains("onlytestrules");
	   	final List<List<String>> aButtons = new LinkedList<>();
	   	
	   	final List<RateInfo> oRates = new LinkedList<>(aRulesByRate.keySet());
	   	Collections.sort(oRates, new Comparator<RateInfo>() {
	   	    @Override
	   	    public int compare(RateInfo oRateInfo1, RateInfo oRateInfo2) {
	   	        return oRateInfo1.toString().compareTo(oRateInfo2.toString());
	   	    }
	   	});
	   	for(final RateInfo oRateInfo : oRates)
	   	{
	   		List<IRule> oRateRules = aRulesByRate.get(oRateInfo);
			final RateInfo oReverseRateInfo = RateInfo.getReverseRate(oRateInfo); 
			
			if (!strType.contains("rate:"))
			{
				final boolean bIsHasRealRule = ManagerUtils.isHasRealRules(oRateInfo) || ManagerUtils.isHasRealRules(oReverseRateInfo);
				if (bIsHasRealRule != bIsShowIfHasReal)
					continue;
				
				String strState = StringUtils.EMPTY;
				for(final IRule oRule : oRateRules)
				{
					final ITradeTask oTradeTask = TradeUtils.getRuleAsTradeTask(oRule);
					final ITradeControler oTradeControler = TradeUtils.getRuleAsTradeControler(oRule);
					if (null != oTradeTask)
						strState += oTradeTask.getTradeInfo().getOrder().getSide() + ";";
					if (null != oTradeControler && !ManagerUtils.isTestObject(oTradeControler))
						strState += (oRule.getRateInfo().getIsReverse() ? "#" : StringUtils.EMPTY) + oTradeControler.getControlerState() + ";";
				}
				strState = (StringUtils.isNotBlank(strState) ?  "[" + strState + "]" : StringUtils.EMPTY);
			
				final BigDecimal nAveragePercent = ManagerUtils.getAverageRateProfitabilityPercent(oRateInfo); 
				final BigDecimal nReversePercent = ManagerUtils.getAverageRateProfitabilityPercent(oReverseRateInfo); 
				
				aButtons.add(Arrays.asList("[" + oRateInfo + "]" + strState + "[" + nAveragePercent + "%][" + nReversePercent + "%]=/rules_rate:" + oRateInfo));
			}
			else
			{		
				oSelectedRateInfo = oRateInfo;
				for(final IRule oRule : oRateRules)
				{
					final String strRuleInfo = oRule.getInfo();
					final BigDecimal nPercent = ManagerUtils.getAverageRateProfitabilityPercent(oRule.getRateInfo()); 
					aButtons.add(Arrays.asList(strRuleInfo + "[" + nPercent + "%]=" + CommandFactory.makeCommandLine(GetTradeInfoCommand.class, GetTradeInfoCommand.RULE_ID_PARAMETER, oRule.getID())));
				}
			}
     	}
		
		if (bIsShowIfHasReal && aButtons.size() < nAllCount)
			aButtons.add(Arrays.asList("#### SHOW TEST RULES ####=" + CommandFactory.makeCommandLine(GetRulesCommand.class, "type", "onlytestrules")));
		
		if (!bIsShowIfHasReal && aButtons.size() < nAllCount)
			aButtons.add(Arrays.asList("#### SHOW REAL RULES ####=" + CommandFactory.makeCommandLine(GetRulesCommand.class, "type", StringUtils.EMPTY)));
			
		String strMessage = "Rules [" + (aButtons.size() > 0 ? aButtons.size() - 1 : 0) + "].";
		if (null != oSelectedRateInfo)
		{
			final BigDecimal nAverageRateProfitabilityPercent = ManagerUtils.getAverageRateProfitabilityPercent(oSelectedRateInfo); 
			final BigDecimal nMinRateHourProfitabilityPercent = ManagerUtils.getMinRateHourProfitabilityPercent(oSelectedRateInfo); 
			
			final StateAnalysisResult oStateAnalysisResult = WorkerFactory.getStockExchange().getLastAnalysisResult();
	    	final RateAnalysisResult oAnalysisResult = oStateAnalysisResult.getRateAnalysisResult(oSelectedRateInfo);
	    	strMessage = GetRateInfoCommand.getRateData(oSelectedRateInfo, oAnalysisResult);
	    	
			strMessage += "Av [" + nAverageRateProfitabilityPercent + " %] Min [" + nMinRateHourProfitabilityPercent + "%]";
			
			final int nHoursCount = ResourceUtils.getIntFromResource("stock.back_view.profitability.hours", WorkerFactory.getStockExchange().getStockProperties(), 3);
			final List<Entry<Integer, RateTradesBlock>> aHoursTrades = ManagerUtils.getHoursTrades(nHoursCount + 1);
			final Map<RateInfo, List<Entry<Integer, TradesBlock>>> aRates = ManagerUtils.convertFromHoursTradesToRateTrades(aHoursTrades);
			
			final IStockManager oManager = WorkerFactory.getStockExchange().getManager();
			final TradesBlock o24HourRateTrade = oManager.getInfo().getRateLast24Hours().getTotal().getRateTrades().get(oSelectedRateInfo);
			strMessage += "\r\n" + (null  != o24HourRateTrade ? "24h " + o24HourRateTrade.asString("only_percent") + ", " : StringUtils.EMPTY);
			final List<Entry<Integer, TradesBlock>> oRateTrades = aRates.get(oSelectedRateInfo);
			if (null != oRateTrades)
			{
				for(final Entry<Integer, TradesBlock> oHourTrades : oRateTrades)
					strMessage += oHourTrades.getKey() + oHourTrades.getValue().asString(TradesBlock.TYPE_SHORT) + ", ";
			}

		}
		WorkerFactory.getMainWorker().sendSystemMessage(strMessage + "BUTTONS\r\n" + TelegramTransport.getButtons(aButtons));
	}
}

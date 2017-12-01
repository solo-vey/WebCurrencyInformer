package solo.model.stocks.item.rules.notify;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.BaseObject;
import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.BaseCommand;
import solo.model.stocks.item.command.GetRateInfoCommand;
import solo.model.stocks.item.command.GetRulesCommand;
import solo.model.stocks.item.command.ICommand;
import solo.model.stocks.item.command.RemoveRuleCommand;
import solo.model.stocks.item.command.SendMessageCommand;
import solo.model.stocks.worker.WorkerFactory;
import solo.model.stocks.worker.WorkerType;
import solo.transport.TransportFactory;
import solo.transport.telegram.TelegramTransport;
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class EventBase extends BaseObject implements IRule
{
	final protected RateInfo m_oRateInfo;
	protected BigDecimal m_nPrice;
	
	public EventBase(final RateInfo oRateInfo, final String strPriceInfo)
	{
		m_oRateInfo = oRateInfo;
		m_nPrice = MathUtils.fromString(CommonUtils.splitFirst(strPriceInfo).toUpperCase());
	}
	
	public String getType()
	{
		return StringUtils.EMPTY;
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return getType() + "/" + m_oRateInfo.getCurrencyFrom() + "/" + MathUtils.toCurrencyString(m_nPrice) + 
			(null != nRuleID ? " " + BaseCommand.getCommand(RemoveRuleCommand.TEMPLATE, nRuleID) : StringUtils.EMPTY);   
	}
	
	public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
	}
	
	public void onOccurred(final BigDecimal nPrice, final Integer nRuleID)
	{
		final String strMessage = getInfo(null) + " is occurred. Price " + MathUtils.toCurrencyString(nPrice) + 
			" " + BaseCommand.getCommand(GetRateInfoCommand.TEMPLATE, m_oRateInfo.getCurrencyFrom()) + 
			" " + BaseCommand.getCommand(GetRulesCommand.NAME);
		final ICommand oSendMessageCommand = new SendMessageCommand(TransportFactory.getTransport(TelegramTransport.NAME), strMessage);
		WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oSendMessageCommand);
		
		if (null != nRuleID)
		{
			final ICommand oDeleteCommand = new RemoveRuleCommand(nRuleID.toString(), true);
			WorkerFactory.getWorker(WorkerType.MAIN).addCommand(oDeleteCommand);
		}
	}
}


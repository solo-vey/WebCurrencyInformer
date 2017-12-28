package solo.model.stocks.item.rules.task;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import solo.model.stocks.analyse.StateAnalysisResult;
import solo.model.stocks.item.IRule;
import solo.model.stocks.item.RateInfo;
import solo.model.stocks.item.command.base.BaseCommand;
import solo.model.stocks.item.command.base.CommandFactory;
import solo.model.stocks.item.command.base.HasParameters;
import solo.model.stocks.item.command.system.GetRateInfoCommand;
import solo.model.stocks.item.command.rule.GetRulesCommand;
import solo.model.stocks.item.command.base.ICommand;
import solo.model.stocks.item.command.rule.RemoveRuleCommand;
import solo.model.stocks.item.command.system.SendMessageCommand;
import solo.model.stocks.worker.WorkerFactory;
import solo.utils.MathUtils;

public class TaskBase extends HasParameters implements IRule
{
	private static final long serialVersionUID = -6534375856366736570L;
	
	final protected RateInfo m_oRateInfo;
	
	public TaskBase(final RateInfo oRateInfo, final String strCommandLine)
	{
		super(strCommandLine, StringUtils.EMPTY);
		m_oRateInfo = oRateInfo;
	}
	
	public TaskBase(final RateInfo oRateInfo, final String strCommandLine, final String strTemplate)
	{
		super(strCommandLine, strTemplate);
		m_oRateInfo = oRateInfo;
	}
	
	public String getHelp(final String strCommandLine)
	{
		return strCommandLine + (StringUtils.isNotBlank(getTemplate()) ? "_" + getTemplate() : StringUtils.EMPTY);
	}
	
	public String getType()
	{
		return StringUtils.EMPTY;
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return getType() + "/" + getCommandLine() + 
			(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}
	
	public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
	}
	
	public void onOccurred(final BigDecimal nPrice, final Integer nRuleID)
	{
		final String strMessage = "Occurred " + getInfo(null) + "/" + MathUtils.toCurrencyString(nPrice) + 
			" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo.getCurrencyFrom()) + 
			" " + BaseCommand.getCommand(GetRulesCommand.NAME);
		final ICommand oSendMessageCommand = new SendMessageCommand(strMessage);
		WorkerFactory.getMainWorker().addCommand(oSendMessageCommand);
	}
	
	@Override public boolean equals(Object obj)
	{
		if (obj instanceof TaskFactory && ((TaskFactory)obj).m_oTaskBase.equals(this))
			return true;
		
		return super.equals(obj);
	}
}


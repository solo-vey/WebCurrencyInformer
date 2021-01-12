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
import solo.utils.CommonUtils;
import solo.utils.MathUtils;

public class TaskBase extends HasParameters implements IRule
{
	private static final long serialVersionUID = -6534375856366736570L;
	
	public static final String RATE_PARAMETER = "#rate#";
	
	protected RateInfo m_oRateInfo;
	protected int m_nID = -1;
	
	public TaskBase(final String strCommandLine)
	{
		super(strCommandLine, RATE_PARAMETER);
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	public TaskBase(final String strCommandLine, final String strTemplate)
	{
		super(strCommandLine, CommonUtils.mergeParameters(RATE_PARAMETER, strTemplate));
		m_oRateInfo = getParameterAsRateInfo(RATE_PARAMETER);
	}
	
	public int getID()
	{
		return m_nID;
	}
	
	public void setID(final int nID)
	{
		m_nID = nID;
	}
	
	public RateInfo getRateInfo()
	{
		return m_oRateInfo;
	}
	
	public String getHelp(final String strCommandLine)
	{
		return strCommandLine + (StringUtils.isNotBlank(getTemplate()) ? "_" + getTemplate() : StringUtils.EMPTY);
	}
	
	public String getType()
	{
		return StringUtils.EMPTY;
	}
	
	public String getInfo()
	{
		return getType() + "/" + getCommandLine() + 
			CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, m_nID);   
	}
	
	public void check(final StateAnalysisResult oStateAnalysisResult)
	{
	}
	
	public void onOccurred(final BigDecimal nPrice, final Integer nRuleID)
	{
		final String strMessage = "Occurred " + getInfo() + "/" + MathUtils.toCurrencyString(nPrice) + 
			" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo) + 
			" " + BaseCommand.getCommand(GetRulesCommand.NAME);
		final ICommand oSendMessageCommand = new SendMessageCommand(strMessage);
		WorkerFactory.getMainWorker().addCommand(oSendMessageCommand);
	}
	
	public void remove()
	{
	}
	
	@Override public boolean equals(Object obj)
	{
		return super.equals(obj);
	}
}


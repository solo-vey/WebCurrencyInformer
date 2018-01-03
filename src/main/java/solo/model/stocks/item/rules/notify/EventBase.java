package solo.model.stocks.item.rules.notify;

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

public class EventBase extends HasParameters implements IRule
{
	private static final long serialVersionUID = -6534375856366736470L;

	final static public String PRICE_PARAMETER = "#price#";
	
	final protected RateInfo m_oRateInfo;
	protected BigDecimal m_nPrice;
	
	public EventBase(final RateInfo oRateInfo, final String strCommandLine)
	{
		this(oRateInfo, strCommandLine, PRICE_PARAMETER);
	}
	
	public EventBase(final RateInfo oRateInfo, final String strCommandLine, final String strTemplate)
	{
		super(strCommandLine, CommonUtils.mergeParameters(PRICE_PARAMETER, strTemplate));
		m_oRateInfo = oRateInfo;
		m_nPrice = getParameterAsBigDecimal(PRICE_PARAMETER);
	}
	
	public String getHelp(final String strCommandStart)
	{
		return strCommandStart + (StringUtils.isNotBlank(getTemplate()) ? "_" + getTemplate() : StringUtils.EMPTY);
	}
	
	public String getType()
	{
		return StringUtils.EMPTY;
	}
	
	public String getInfo(final Integer nRuleID)
	{
		return getType() + "/" + m_oRateInfo + "/" + MathUtils.toCurrencyString(m_nPrice) + 
			(null != nRuleID ? " " + CommandFactory.makeCommandLine(RemoveRuleCommand.class, RemoveRuleCommand.ID_PARAMETER, nRuleID) : StringUtils.EMPTY);   
	}
	
	public void check(final StateAnalysisResult oStateAnalysisResult, final Integer nRuleID)
	{
	}
	
	public void remove()
	{
	}
	
	public void onOccurred(final BigDecimal nPrice, final Integer nRuleID)
	{
		final String strMessage = "Occurred " + getInfo(null) + "/" + MathUtils.toCurrencyString(nPrice) + 
 			" " + CommandFactory.makeCommandLine(GetRateInfoCommand.class, GetRateInfoCommand.RATE_PARAMETER, m_oRateInfo) + 
			" " + BaseCommand.getCommand(GetRulesCommand.NAME);
		final ICommand oSendMessageCommand = new SendMessageCommand(strMessage);
		WorkerFactory.getMainWorker().addCommand(oSendMessageCommand);
		
		if (null != nRuleID)
		{
			final ICommand oDeleteCommand = new RemoveRuleCommand(nRuleID.toString(), true);
			WorkerFactory.getMainWorker().addCommand(oDeleteCommand);
		}
	}
}

